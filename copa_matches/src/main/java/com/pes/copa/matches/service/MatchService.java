package com.pes.copa.matches.service;

import com.pes.copa.matches.client.TournamentClient;
import com.pes.copa.matches.client.TeamsClient;
import com.pes.copa.matches.client.StatsClient;
import com.pes.copa.matches.dto.external.TournamentDTO;
import com.pes.copa.matches.dto.external.TournamentTeamDTO;
import com.pes.copa.matches.dto.external.TeamBasicDTO;
import com.pes.copa.matches.dto.request.GoalDTO;
import com.pes.copa.matches.dto.request.SimulateMatchesDTO;
import com.pes.copa.matches.dto.request.SubmitMatchResultDTO;
import com.pes.copa.matches.dto.response.GroupStandingDTO;
import com.pes.copa.matches.dto.response.MatchDTO;
import com.pes.copa.matches.dto.response.PendingMatchesDTO;
import com.pes.copa.matches.dto.response.TeamBasicDTO as ResponseTeamBasicDTO;
import com.pes.copa.matches.dto.response.TeamStandingDTO;
import com.pes.copa.matches.entity.Match;
import com.pes.copa.matches.entity.MatchGoal;
import com.pes.copa.matches.enums.MatchPhase;
import com.pes.copa.matches.enums.MatchStatus;
import com.pes.copa.matches.repository.MatchGoalRepository;
import com.pes.copa.matches.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MatchService {
    
    private final MatchRepository matchRepository;
    private final MatchGoalRepository matchGoalRepository;
    private final TournamentClient tournamentClient;
    private final TeamsClient teamsClient;
    private final StatsClient statsClient;
    
    @Autowired
    public MatchService(MatchRepository matchRepository, 
                       MatchGoalRepository matchGoalRepository,
                       TournamentClient tournamentClient,
                       TeamsClient teamsClient,
                       StatsClient statsClient) {
        this.matchRepository = matchRepository;
        this.matchGoalRepository = matchGoalRepository;
        this.tournamentClient = tournamentClient;
        this.teamsClient = teamsClient;
        this.statsClient = statsClient;
    }
    
    /**
     * Genera todos los partidos para un torneo según su formato
     */
    public void generateMatches(Long tournamentId) {
        // ✅ Obtener datos de otros servicios via cliente
        TournamentDTO tournament = tournamentClient.getTournament(tournamentId);
        List<TournamentTeamDTO> teams = tournamentClient.getTournamentTeams(tournamentId);
        
        if ("GROUP_STAGE_KNOCKOUT".equals(tournament.getTournamentFormat())) {
            generateGroupStageMatches(tournamentId, teams);
            generateKnockoutMatches(tournamentId, tournament.getNumTeams());
        } else if ("DIRECT_KNOCKOUT".equals(tournament.getTournamentFormat())) {
            generateDirectKnockoutMatches(tournamentId, teams);
        }
        
        // ✅ Notificar a Stats Service para inicializar estadísticas
        try {
            statsClient.initializeTournamentStats(tournamentId, teams);
        } catch (Exception e) {
            // Log error pero no fallar la operación principal
            System.err.println("Failed to initialize stats: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene partidos pendientes de un jugador específico
     * ✅ Estrategia: Obtener equipos del jugador desde Tournament Service, 
     * luego consultar partidos localmente
     */
    public List<MatchDTO> getPendingMatchesByPlayer(Long playerId, Long tournamentId) {
        // Obtener equipos del jugador desde Tournament Service
        List<TournamentTeamDTO> playerTeams = tournamentClient.getTeamsByPlayer(tournamentId, playerId);
        
        List<Long> teamIds = playerTeams.stream()
                .map(TournamentTeamDTO::getTeamId)
                .collect(Collectors.toList());
        
        // Consultar partidos localmente
        List<Match> allPendingMatches = matchRepository.findByTournamentIdAndStatus(tournamentId, MatchStatus.PENDING);
        
        List<Match> playerMatches = allPendingMatches.stream()
                .filter(match -> teamIds.contains(match.getHomeTeamId()) || teamIds.contains(match.getAwayTeamId()))
                .collect(Collectors.toList());
        
        return playerMatches.stream()
                .map(this::convertToMatchDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los partidos pendientes del torneo agrupados por jugador
     */
    public List<PendingMatchesDTO> getAllPendingMatches(Long tournamentId) {
        List<Match> pendingMatches = matchRepository.findPendingPlayerMatches(tournamentId);
        
        // ✅ Obtener información de equipos y jugadores desde otros servicios
        List<TournamentTeamDTO> allTeams = tournamentClient.getTournamentTeams(tournamentId);
        
        Map<Long, List<Match>> matchesByPlayer = new HashMap<>();
        
        for (Match match : pendingMatches) {
            Long homePlayerId = getPlayerIdForTeam(allTeams, match.getHomeTeamId());
            Long awayPlayerId = getPlayerIdForTeam(allTeams, match.getAwayTeamId());
            
            if (homePlayerId != null) {
                matchesByPlayer.computeIfAbsent(homePlayerId, k -> new ArrayList<>()).add(match);
            }
            if (awayPlayerId != null && !Objects.equals(homePlayerId, awayPlayerId)) {
                matchesByPlayer.computeIfAbsent(awayPlayerId, k -> new ArrayList<>()).add(match);
            }
        }
        
        return matchesByPlayer.entrySet().stream()
                .map(entry -> {
                    PendingMatchesDTO dto = new PendingMatchesDTO();
                    dto.setPlayerId(entry.getKey());
                    // ✅ Aquí deberías consultar Users Service para nombre real
                    dto.setPlayerName("Player " + entry.getKey());
                    dto.setPendingMatches(entry.getValue().stream()
                            .map(this::convertToMatchDTO)
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Registra el resultado de un partido
     */
    public MatchDTO submitMatchResult(SubmitMatchResultDTO dto) {
        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));
        
        if (match.getStatus() != MatchStatus.PENDING) {
            throw new RuntimeException("Match is not pending");
        }
        
        // Actualizar resultado
        match.setHomeScore(dto.getHomeScore());
        match.setAwayScore(dto.getAwayScore());
        match.setHomePenalties(dto.getHomePenalties());
        match.setAwayPenalties(dto.getAwayPenalties());
        match.setStatus(MatchStatus.FINISHED);
        match.setPlayedDate(LocalDateTime.now());
        
        Match savedMatch = matchRepository.save(match);
        
        // Guardar goles
        if (dto.getGoals() != null && !dto.getGoals().isEmpty()) {
            saveMatchGoals(savedMatch.getId(), dto.getGoals());
        }
        
        // ✅ Notificar resultado a Stats Service de forma asíncrona
        try {
            statsClient.processMatchResult(buildMatchResultDTO(savedMatch));
        } catch (Exception e) {
            // Log error pero no fallar la operación principal
            System.err.println("Failed to update stats: " + e.getMessage());
        }
        
        return convertToMatchDTO(savedMatch);
    }
    
    // ✅ Método auxiliar para comunicar con Stats Service
    private MatchResultDTO buildMatchResultDTO(Match match) {
        // Obtener información de jugadores desde Tournament Service
        List<TournamentTeamDTO> teams = tournamentClient.getTournamentTeams(match.getTournamentId());
        
        Long homePlayerId = getPlayerIdForTeam(teams, match.getHomeTeamId());
        Long awayPlayerId = getPlayerIdForTeam(teams, match.getAwayTeamId());
        
        MatchResultDTO dto = new MatchResultDTO();
        dto.setMatchId(match.getId());
        dto.setTournamentId(match.getTournamentId());
        dto.setHomeTeamId(match.getHomeTeamId());
        dto.setAwayTeamId(match.getAwayTeamId());
        dto.setHomeScore(match.getHomeScore());
        dto.setAwayScore(match.getAwayScore());
        dto.setPhase(match.getPhase().name());
        dto.setGroupName(match.getGroupName());
        dto.setHomePlayerId(homePlayerId);
        dto.setAwayPlayerId(awayPlayerId);
        
        return dto;
    }
    
    // ... resto de métodos igual que antes ...
    
    /**
     * ✅ Convierte Match a DTO consultando información de otros servicios
     */
    private MatchDTO convertToMatchDTO(Match match) {
        MatchDTO dto = new MatchDTO();
        dto.setId(match.getId());
        dto.setTournamentId(match.getTournamentId());
        dto.setPhase(match.getPhase().name());
        dto.setGroupName(match.getGroupName());
        dto.setMatchday(match.getMatchday());
        dto.setHomeScore(match.getHomeScore());
        dto.setAwayScore(match.getAwayScore());
        dto.setHomePenalties(match.getHomePenalties());
        dto.setAwayPenalties(match.getAwayPenalties());
        dto.setStatus(match.getStatus().name());
        dto.setRequiresPlayerInput(match.getRequiresPlayerInput());
        dto.setPlayedDate(match.getPlayedDate());
        
        // ✅ Consultar información de equipos desde Teams Service
        try {
            // Primero obtener el tipo de equipo desde Tournament Service
            List<TournamentTeamDTO> tournamentTeams = tournamentClient.getTournamentTeams(match.getTournamentId());
            
            TournamentTeamDTO homeTeamInfo = tournamentTeams.stream()
                    .filter(t -> Objects.equals(t.getTeamId(), match.getHomeTeamId()))
                    .findFirst()
                    .orElse(null);
            
            TournamentTeamDTO awayTeamInfo = tournamentTeams.stream()
                    .filter(t -> Objects.equals(t.getTeamId(), match.getAwayTeamId()))
                    .findFirst()
                    .orElse(null);
            
            if (homeTeamInfo != null) {
                TeamBasicDTO homeTeamBasic = getTeamBasicInfo(homeTeamInfo);
                ResponseTeamBasicDTO homeTeam = new ResponseTeamBasicDTO();
                homeTeam.setId(homeTeamBasic.getId());
                homeTeam.setName(homeTeamBasic.getName());
                homeTeam.setLogoUrl(homeTeamBasic.getLogoUrl());
                homeTeam.setPlayerId(homeTeamInfo.getPlayerId());
                dto.setHomeTeam(homeTeam);
            }
            
            if (awayTeamInfo != null) {
                TeamBasicDTO awayTeamBasic = getTeamBasicInfo(awayTeamInfo);
                ResponseTeamBasicDTO awayTeam = new ResponseTeamBasicDTO();
                awayTeam.setId(awayTeamBasic.getId());
                awayTeam.setName(awayTeamBasic.getName());
                awayTeam.setLogoUrl(awayTeamBasic.getLogoUrl());
                awayTeam.setPlayerId(awayTeamInfo.getPlayerId());
                dto.setAwayTeam(awayTeam);
            }
            
        } catch (Exception e) {
            // Log error pero devolver DTO con información mínima
            System.err.println("Failed to get team info: " + e.getMessage());
        }
        
        // Obtener goles
        List<MatchGoal> goals = matchGoalRepository.findByMatchIdOrderByMinute(match.getId());
        dto.setGoals(goals.stream()
                .map(this::convertToMatchGoalDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    private TeamBasicDTO getTeamBasicInfo(TournamentTeamDTO tournamentTeam) {
        if ("COUNTRY".equals(tournamentTeam.getTeamType())) {
            return teamsClient.getCountryBasicInfo(tournamentTeam.getTeamId());
        } else {
            return teamsClient.getTeamBasicInfo(tournamentTeam.getTeamId());
        }
    }
}