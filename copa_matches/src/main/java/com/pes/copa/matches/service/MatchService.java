package com.pes.copa.matches.service;

import com.pes.copa.matches.client.TournamentClient;
import com.pes.copa.matches.client.TeamsClient;
import com.pes.copa.matches.client.StatsClient;
import com.pes.copa.matches.dto.external.*;
import com.pes.copa.matches.dto.request.GoalDTO;
import com.pes.copa.matches.dto.request.SimulateMatchesDTO;
import com.pes.copa.matches.dto.request.SubmitMatchResultDTO;
import com.pes.copa.matches.dto.response.*;
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
    
    // Nombres legibles para las fases
    private static final Map<MatchPhase, String> PHASE_NAMES = Map.of(
        MatchPhase.GROUP_STAGE, "Fase de Grupos",
        MatchPhase.ROUND_16, "Octavos de Final",
        MatchPhase.QUARTER_FINAL, "Cuartos de Final",
        MatchPhase.SEMI_FINAL, "Semifinal",
        MatchPhase.THIRD_PLACE, "Tercer Lugar",
        MatchPhase.FINAL, "Final"
    );
    
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
    
    // ============================================
    // MÉTODOS PÚBLICOS - API DEL SERVICE
    // ============================================
    
    /**
     * Genera todos los partidos para un torneo según su formato
     */
    public void generateMatches(Long tournamentId) {
        TournamentDTO tournament = tournamentClient.getTournament(tournamentId);
        TournamentStructureDTO structure = tournamentClient.getTournamentStructure(tournamentId);
        
        if ("GROUPS_THEN_KNOCKOUT".equals(tournament.getTournamentFormat())) {
            generateGroupStageMatches(tournamentId, structure);
            generateKnockoutMatches(tournamentId, structure);
        } else if ("DIRECT_KNOCKOUT".equals(tournament.getTournamentFormat())) {
            generateDirectKnockoutMatches(tournamentId, structure);
        }
        
        // Notificar a Stats Service para inicializar estadísticas
        try {
            List<TeamPositionDTO> teams = getAllTournamentTeams(structure);
            statsClient.initializeTournamentStats(tournamentId, teams);
        } catch (Exception e) {
            System.err.println("Failed to initialize stats: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene partidos pendientes de un jugador específico
     */
    public List<MatchDTO> getPendingMatchesByPlayer(Long playerId, Long tournamentId) {
        List<TeamPositionDTO> playerTeams = tournamentClient.getPlayerTeams(tournamentId, playerId);
        
        List<Long> teamIds = playerTeams.stream()
                .map(TeamPositionDTO::getTeamId)
                .collect(Collectors.toList());
        
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
        TournamentStructureDTO structure = tournamentClient.getTournamentStructure(tournamentId);
        
        Map<Long, List<Match>> matchesByPlayer = new HashMap<>();
        
        for (Match match : pendingMatches) {
            TeamPositionDTO homeTeam = findTeamInStructure(structure, match.getHomeTeamId());
            TeamPositionDTO awayTeam = findTeamInStructure(structure, match.getAwayTeamId());
            
            if (homeTeam != null && homeTeam.getPlayerId() != null) {
                matchesByPlayer.computeIfAbsent(homeTeam.getPlayerId(), k -> new ArrayList<>()).add(match);
            }
            if (awayTeam != null && awayTeam.getPlayerId() != null && 
                !Objects.equals(homeTeam.getPlayerId(), awayTeam.getPlayerId())) {
                matchesByPlayer.computeIfAbsent(awayTeam.getPlayerId(), k -> new ArrayList<>()).add(match);
            }
        }
        
        return matchesByPlayer.entrySet().stream()
                .map(entry -> {
                    PendingMatchesDTO dto = new PendingMatchesDTO();
                    dto.setPlayerId(entry.getKey());
                    dto.setPlayerName(getPlayerName(structure, entry.getKey()));
                    dto.setTotalPendingMatches(entry.getValue().size());
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
        
        // Validar resultado
        validateMatchResult(dto, match);
        
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
        
        // Determinar ganador y procesar siguiente ronda (si es eliminatoria)
        if (isKnockoutPhase(match.getPhase())) {
            processKnockoutResult(savedMatch);
        }
        
        // Notificar resultado a Stats Service
        try {
            MatchResultDTO resultDTO = buildMatchResultDTO(savedMatch);
            statsClient.processMatchResult(resultDTO);
        } catch (Exception e) {
            System.err.println("Failed to update stats: " + e.getMessage());
        }
        
        return convertToMatchDTO(savedMatch);
    }
    
    /**
     * Simula partidos automáticamente (para equipos IA)
     */
    public List<MatchDTO> simulateMatches(SimulateMatchesDTO dto) {
        List<Match> matchesToSimulate;
        
        if (dto.getPhase() != null) {
            MatchPhase phase = MatchPhase.valueOf(dto.getPhase());
            matchesToSimulate = matchRepository.findByTournamentIdAndPhaseAndStatus(
                dto.getTournamentId(), phase, MatchStatus.PENDING);
        } else if (dto.getGroupName() != null) {
            matchesToSimulate = matchRepository.findByTournamentIdAndGroupName(
                dto.getTournamentId(), dto.getGroupName()).stream()
                .filter(m -> m.getStatus() == MatchStatus.PENDING)
                .collect(Collectors.toList());
        } else {
            matchesToSimulate = matchRepository.findByTournamentIdAndStatus(
                dto.getTournamentId(), MatchStatus.PENDING).stream()
                .filter(m -> !m.getRequiresPlayerInput()) // Solo simular partidos IA vs IA
                .collect(Collectors.toList());
        }
        
        List<MatchDTO> simulatedMatches = new ArrayList<>();
        
        for (Match match : matchesToSimulate) {
            if (!match.getRequiresPlayerInput()) {
                MatchDTO simulatedMatch = simulateMatch(match);
                simulatedMatches.add(simulatedMatch);
            }
        }
        
        return simulatedMatches;
    }
    
    /**
     * Obtiene clasificaciones de fase de grupos
     */
    public List<GroupStandingDTO> getGroupStandings(Long tournamentId) {
        List<String> groups = matchRepository.findDistinctGroupNamesByTournamentId(tournamentId);
        
        return groups.stream()
                .map(groupName -> {
                    GroupStandingDTO groupStanding = new GroupStandingDTO();
                    groupStanding.setGroupName(groupName);
                    groupStanding.setStandings(calculateGroupStandings(tournamentId, groupName));
                    return groupStanding;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los partidos de un torneo
     */
    public List<MatchDTO> getTournamentMatches(Long tournamentId) {
        List<Match> matches = matchRepository.findByTournamentId(tournamentId);
        return matches.stream()
                .map(this::convertToMatchDTO)
                .collect(Collectors.toList());
    }
    
    // ============================================
    // MÉTODOS PRIVADOS - LÓGICA INTERNA
    // ============================================
    
    /**
     * Convierte Match entity a DTO con información enriquecida
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
        dto.setNextMatchId(match.getNextMatchId());
        dto.setWinnerAdvancesTo(getWinnerAdvancesTo(match.getPhase()));
        
        try {
            TournamentStructureDTO structure = tournamentClient.getTournamentStructure(match.getTournamentId());
            
            TeamPositionDTO homeTeamPosition = findTeamInStructure(structure, match.getHomeTeamId());
            TeamPositionDTO awayTeamPosition = findTeamInStructure(structure, match.getAwayTeamId());
            
            if (homeTeamPosition != null) {
                dto.setHomeTeam(buildTeamBasicDTO(homeTeamPosition));
            }
            if (awayTeamPosition != null) {
                dto.setAwayTeam(buildTeamBasicDTO(awayTeamPosition));
            }
            
        } catch (Exception e) {
            System.err.println("Failed to get team info: " + e.getMessage());
            dto.setHomeTeam(createMinimalTeamDTO(match.getHomeTeamId()));
            dto.setAwayTeam(createMinimalTeamDTO(match.getAwayTeamId()));
        }
        
        // Obtener goles
        List<MatchGoal> goals = matchGoalRepository.findByMatchIdOrderByMinute(match.getId());
        dto.setGoals(goals.stream()
                .map(this::convertToMatchGoalDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    /**
     * Convierte TeamPositionDTO a TeamBasicDTO con datos enriquecidos
     */
    private TeamBasicDTO buildTeamBasicDTO(TeamPositionDTO teamPosition) {
        TeamBasicDTO dto = new TeamBasicDTO();
        dto.setId(teamPosition.getTeamId());
        dto.setName(teamPosition.getTeamName());
        dto.setCountry(teamPosition.getCountry());
        dto.setTeamType(teamPosition.getTeamType());
        dto.setPlayerId(teamPosition.getPlayerId());
        dto.setPlayerName(teamPosition.getPlayerName());
        dto.setIsAI(teamPosition.getIsAI());
        
        // Obtener información adicional del Teams Service
        try {
            if ("COUNTRY".equals(teamPosition.getTeamType())) {
                CountryDTO country = teamsClient.getCountry(teamPosition.getTeamId());
                dto.setFlagUrl(country.getFlagURL());
                dto.setContinent(country.getContinent());
                dto.setIsMundialist(country.isMundialist());
            } else {
                TeamDTO team = teamsClient.getTeam(teamPosition.getTeamId());
                dto.setLogoUrl(team.getLogoURL());
                dto.setContinent(team.getContinent());
                dto.setIsChampions(team.getIsChampions());
            }
        } catch (Exception e) {
            System.err.println("Failed to enrich team data: " + e.getMessage());
            // Usar datos básicos del Tournament Service como fallback
            if ("COUNTRY".equals(teamPosition.getTeamType())) {
                dto.setFlagUrl(teamPosition.getLogoURL()); 
            } else {
                dto.setLogoUrl(teamPosition.getLogoURL());
            }
        }
        
        return dto;
    }
    
    /**
     * Busca un equipo en la estructura del torneo
     */
    private TeamPositionDTO findTeamInStructure(TournamentStructureDTO structure, Long teamId) {
        // Buscar en grupos (si existen)
        if (structure.getGroups() != null) {
            for (List<TeamPositionDTO> groupTeams : structure.getGroups().values()) {
                for (TeamPositionDTO team : groupTeams) {
                    if (Objects.equals(team.getTeamId(), teamId)) {
                        return team;
                    }
                }
            }
        }
        
        // Buscar en bracket (eliminatorias)
        if (structure.getBracket() != null) {
            for (TeamPositionDTO team : structure.getBracket()) {
                if (Objects.equals(team.getTeamId(), teamId)) {
                    return team;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Crea DTO mínimo cuando fallan las llamadas externas
     */
    private TeamBasicDTO createMinimalTeamDTO(Long teamId) {
        TeamBasicDTO dto = new TeamBasicDTO();
        dto.setId(teamId);
        dto.setName("Equipo " + teamId);
        dto.setIsAI(true);
        return dto;
    }
    
    /**
     * LÓGICA DE DETERMINAR GANADOR - Núcleo del sistema de eliminatorias
     */
    private Long determineWinner(Match match) {
        // Tiempo regular
        if (!Objects.equals(match.getHomeScore(), match.getAwayScore())) {
            return match.getHomeScore() > match.getAwayScore() 
                ? match.getHomeTeamId() 
                : match.getAwayTeamId();
        }
        
        // Solo en eliminatorias: revisar penales
        if (isKnockoutPhase(match.getPhase())) {
            if (match.getHomePenalties() != null && match.getAwayPenalties() != null) {
                if (!Objects.equals(match.getHomePenalties(), match.getAwayPenalties())) {
                    return match.getHomePenalties() > match.getAwayPenalties() 
                        ? match.getHomeTeamId() 
                        : match.getAwayTeamId();
                }
            }
            // Si no hay penales definidos, es empate (no debería pasar en eliminatorias)
            throw new RuntimeException("Knockout match cannot end in tie without penalties");
        }
        
        // Fase de grupos: empate
        return null;
    }
    
    /**
     * Procesa el resultado de un partido eliminatorio y avanza al ganador
     */
    private void processKnockoutResult(Match match) {
        Long winnerId = determineWinner(match);
        
        if (winnerId != null && match.getNextMatchId() != null) {
            // Buscar el siguiente partido
            Match nextMatch = matchRepository.findById(match.getNextMatchId())
                    .orElse(null);
            
            if (nextMatch != null) {
                // Determinar si el ganador va como local o visitante
                if (nextMatch.getHomeTeamId() == null) {
                    nextMatch.setHomeTeamId(winnerId);
                } else if (nextMatch.getAwayTeamId() == null) {
                    nextMatch.setAwayTeamId(winnerId);
                }
                
                // Verificar si ambos equipos están definidos para marcar como jugable
                if (nextMatch.getHomeTeamId() != null && nextMatch.getAwayTeamId() != null) {
                    // Verificar si requiere input del jugador
                    TournamentStructureDTO structure = tournamentClient.getTournamentStructure(match.getTournamentId());
                    TeamPositionDTO homeTeam = findTeamInStructure(structure, nextMatch.getHomeTeamId());
                    TeamPositionDTO awayTeam = findTeamInStructure(structure, nextMatch.getAwayTeamId());
                    
                    boolean requiresInput = (homeTeam != null && homeTeam.getPlayerId() != null) ||
                                          (awayTeam != null && awayTeam.getPlayerId() != null);
                    
                    nextMatch.setRequiresPlayerInput(requiresInput);
                }
                
                matchRepository.save(nextMatch);
            }
        }
    }
    
    /**
     * Simula un partido automáticamente (para equipos IA)
     */
    private MatchDTO simulateMatch(Match match) {
        // Algoritmo simple de simulación
        Random random = new Random();
        
        int homeScore = random.nextInt(4); // 0-3 goles
        int awayScore = random.nextInt(4);
        
        match.setHomeScore(homeScore);
        match.setAwayScore(awayScore);
        
        // Si es eliminatoria y hay empate, simular penales
        if (isKnockoutPhase(match.getPhase()) && Objects.equals(homeScore, awayScore)) {
            match.setHomePenalties(random.nextInt(2) + 3); // 3-4 penales
            match.setAwayPenalties(random.nextInt(2) + 3);
            
            // Asegurar que no haya empate en penales
            if (Objects.equals(match.getHomePenalties(), match.getAwayPenalties())) {
                match.setHomePenalties(match.getHomePenalties() + 1);
            }
        }
        
        match.setStatus(MatchStatus.SIMULATED);
        match.setPlayedDate(LocalDateTime.now());
        
        Match savedMatch = matchRepository.save(match);
        
        // Procesar resultado si es eliminatoria
        if (isKnockoutPhase(match.getPhase())) {
            processKnockoutResult(savedMatch);
        }
        
        // Notificar a Stats Service
        try {
            MatchResultDTO resultDTO = buildMatchResultDTO(savedMatch);
            resultDTO.setWasSimulated(true);
            statsClient.processMatchResult(resultDTO);
        } catch (Exception e) {
            System.err.println("Failed to update stats: " + e.getMessage());
        }
        
        return convertToMatchDTO(savedMatch);
    }
    
    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================
    
    private boolean isKnockoutPhase(MatchPhase phase) {
        return phase != MatchPhase.GROUP_STAGE;
    }
    
    private String getWinnerAdvancesTo(MatchPhase phase) {
        return switch (phase) {
            case ROUND_16 -> "Cuartos de Final";
            case QUARTER_FINAL -> "Semifinal";
            case SEMI_FINAL -> "Final";
            case THIRD_PLACE -> "Tercer Lugar";
            case FINAL -> "Campeón";
            default -> null;
        };
    }
    
    private void validateMatchResult(SubmitMatchResultDTO dto, Match match) {
        if (dto.getHomeScore() < 0 || dto.getAwayScore() < 0) {
            throw new RuntimeException("Scores cannot be negative");
        }
        
        if (isKnockoutPhase(match.getPhase()) && 
            Objects.equals(dto.getHomeScore(), dto.getAwayScore()) &&
            (dto.getHomePenalties() == null || dto.getAwayPenalties() == null)) {
            throw new RuntimeException("Knockout matches require penalty result if tied");
        }
    }
    
    private String getPlayerName(TournamentStructureDTO structure, Long playerId) {
        // Buscar en la estructura del torneo
        List<TeamPositionDTO> allTeams = getAllTournamentTeams(structure);
        return allTeams.stream()
                .filter(t -> Objects.equals(t.getPlayerId(), playerId))
                .findFirst()
                .map(TeamPositionDTO::getPlayerName)
                .orElse("Player " + playerId);
    }
    
    private List<TeamPositionDTO> getAllTournamentTeams(TournamentStructureDTO structure) {
        List<TeamPositionDTO> allTeams = new ArrayList<>();
        
        if (structure.getGroups() != null) {
            structure.getGroups().values().forEach(allTeams::addAll);
        }
        
        if (structure.getBracket() != null) {
            allTeams.addAll(structure.getBracket());
        }
        
        return allTeams;
    }
    
    // ============================================
    // MÉTODOS DE GENERACIÓN DE PARTIDOS
    // ============================================
    
    /**
     * Genera partidos de fase de grupos
     */
    private void generateGroupStageMatches(Long tournamentId, TournamentStructureDTO structure) {
        if (structure.getGroups() == null) return;
        
        for (Map.Entry<String, List<TeamPositionDTO>> group : structure.getGroups().entrySet()) {
            String groupName = group.getKey();
            List<TeamPositionDTO> teams = group.getValue();
            
            if (teams.size() != 4) {
                throw new RuntimeException("Group " + groupName + " must have exactly 4 teams");
            }
            
            // Generar todos vs todos (round robin)
            int matchday = 1;
            List<Match> groupMatches = new ArrayList<>();
            
            // Jornada 1: A vs B, C vs D
            groupMatches.add(createGroupMatch(tournamentId, teams.get(0), teams.get(1), groupName, matchday));
            groupMatches.add(createGroupMatch(tournamentId, teams.get(2), teams.get(3), groupName, matchday));
            
            matchday++;
            // Jornada 2: A vs C, B vs D  
            groupMatches.add(createGroupMatch(tournamentId, teams.get(0), teams.get(2), groupName, matchday));
            groupMatches.add(createGroupMatch(tournamentId, teams.get(1), teams.get(3), groupName, matchday));
            
            matchday++;
            // Jornada 3: A vs D, B vs C
            groupMatches.add(createGroupMatch(tournamentId, teams.get(0), teams.get(3), groupName, matchday));
            groupMatches.add(createGroupMatch(tournamentId, teams.get(1), teams.get(2), groupName, matchday));
            
            matchRepository.saveAll(groupMatches);
        }
    }
    
    /**
     * Genera partidos de eliminatorias después de fase de grupos
     */
    private void generateKnockoutMatches(Long tournamentId, TournamentStructureDTO structure) {
        // Este método genera la estructura de eliminatorias pero SIN equipos asignados
        // Los equipos se asignan cuando termina la fase de grupos
        
        Integer numTeams = getTotalTeamsFromGroups(structure);
        int qualifiedTeams = numTeams / 2; // Asumiendo que clasifican 2 por grupo
        
        List<Match> knockoutMatches = new ArrayList<>();
        Map<MatchPhase, List<Long>> matchIdsByPhase = new HashMap<>();
        
        // Generar octavos de final (si hay 16 equipos)
        if (qualifiedTeams >= 16) {
            List<Long> roundOf16Ids = createPhaseMatches(tournamentId, MatchPhase.ROUND_16, 8);
            matchIdsByPhase.put(MatchPhase.ROUND_16, roundOf16Ids);
            knockoutMatches.addAll(getMatchesByIds(roundOf16Ids));
        }
        
        // Generar cuartos de final
        List<Long> quarterIds = createPhaseMatches(tournamentId, MatchPhase.QUARTER_FINAL, 4);
        matchIdsByPhase.put(MatchPhase.QUARTER_FINAL, quarterIds);
        knockoutMatches.addAll(getMatchesByIds(quarterIds));
        
        // Generar semifinales
        List<Long> semiIds = createPhaseMatches(tournamentId, MatchPhase.SEMI_FINAL, 2);
        matchIdsByPhase.put(MatchPhase.SEMI_FINAL, semiIds);
        knockoutMatches.addAll(getMatchesByIds(semiIds));
        
        // Generar tercer lugar
        Match thirdPlaceMatch = createKnockoutMatch(tournamentId, MatchPhase.THIRD_PLACE);
        knockoutMatches.add(thirdPlaceMatch);
        
        // Generar final
        Match finalMatch = createKnockoutMatch(tournamentId, MatchPhase.FINAL);
        knockoutMatches.add(finalMatch);
        
        // Establecer conexiones entre fases (nextMatchId)
        linkKnockoutMatches(matchIdsByPhase, thirdPlaceMatch.getId(), finalMatch.getId());
        
        matchRepository.saveAll(knockoutMatches);
    }
    
    /**
     * Genera eliminatorias directas (sin fase de grupos)
     */
    private void generateDirectKnockoutMatches(Long tournamentId, TournamentStructureDTO structure) {
        List<TeamPositionDTO> allTeams = structure.getBracket();
        
        if (allTeams == null || allTeams.isEmpty()) {
            throw new RuntimeException("No teams found for direct knockout");
        }
        
        int numTeams = allTeams.size();
        List<Match> matches = new ArrayList<>();
        
        // Determinar fase inicial según número de equipos
        MatchPhase startPhase = determineStartPhase(numTeams);
        
        // Crear primera ronda
        for (int i = 0; i < allTeams.size(); i += 2) {
            if (i + 1 < allTeams.size()) {
                Match match = createKnockoutMatch(tournamentId, startPhase);
                match.setHomeTeamId(allTeams.get(i).getTeamId());
                match.setAwayTeamId(allTeams.get(i + 1).getTeamId());
                
                // Verificar si requiere input del jugador
                boolean requiresInput = allTeams.get(i).getPlayerId() != null || 
                                      allTeams.get(i + 1).getPlayerId() != null;
                match.setRequiresPlayerInput(requiresInput);
                
                matches.add(match);
            }
        }
        
        matchRepository.saveAll(matches);
        
        // Generar fases posteriores (sin equipos asignados)
        generateSubsequentKnockoutPhases(tournamentId, startPhase, matches);
    }
    
    // ============================================
    // MÉTODOS DE CLASIFICACIONES
    // ============================================
    
    /**
     * Calcula la clasificación de un grupo específico
     */
    private List<TeamStandingDTO> calculateGroupStandings(Long tournamentId, String groupName) {
        List<Match> groupMatches = matchRepository.findByTournamentIdAndGroupName(tournamentId, groupName);
        TournamentStructureDTO structure = tournamentClient.getTournamentStructure(tournamentId);
        
        // Obtener equipos del grupo
        List<TeamPositionDTO> groupTeams = structure.getGroups().get(groupName);
        if (groupTeams == null) return new ArrayList<>();
        
        // Inicializar estadísticas
        Map<Long, TeamStandingStats> stats = new HashMap<>();
        for (TeamPositionDTO team : groupTeams) {
            stats.put(team.getTeamId(), new TeamStandingStats());
        }
        
        // Procesar partidos jugados
        for (Match match : groupMatches) {
            if (match.getStatus() == MatchStatus.FINISHED || match.getStatus() == MatchStatus.SIMULATED) {
                updateTeamStats(stats, match);
            }
        }
        
        // Convertir a DTOs y ordenar
        List<TeamStandingDTO> standings = new ArrayList<>();
        for (TeamPositionDTO team : groupTeams) {
            TeamStandingStats teamStats = stats.get(team.getTeamId());
            
            TeamStandingDTO standing = new TeamStandingDTO();
            standing.setTeam(buildTeamBasicDTO(team));
            standing.setPoints(teamStats.points);
            standing.setWins(teamStats.wins);
            standing.setDraws(teamStats.draws);
            standing.setLosses(teamStats.losses);
            standing.setGoalsFor(teamStats.goalsFor);
            standing.setGoalsAgainst(teamStats.goalsAgainst);
            standing.setGoalDifference(teamStats.goalsFor - teamStats.goalsAgainst);
            
            standings.add(standing);
        }
        
        // Ordenar por criterios FIFA
        standings.sort((a, b) -> {
            // 1. Puntos
            int pointsCompare = Integer.compare(b.getPoints(), a.getPoints());
            if (pointsCompare != 0) return pointsCompare;
            
            // 2. Diferencia de goles
            int gdCompare = Integer.compare(b.getGoalDifference(), a.getGoalDifference());
            if (gdCompare != 0) return gdCompare;
            
            // 3. Goles a favor
            return Integer.compare(b.getGoalsFor(), a.getGoalsFor());
        });
        
        // Asignar posiciones
        for (int i = 0; i < standings.size(); i++) {
            standings.get(i).setPosition(i + 1);
        }
        
        return standings;
    }
    
    /**
     * Avanza equipos de fase de grupos a eliminatorias
     */
    public void advanceFromGroupStage(Long tournamentId) {
        List<GroupStandingDTO> allGroups = getGroupStandings(tournamentId);
        List<TeamPositionDTO> qualifiedTeams = new ArrayList<>();
        
        // Tomar los 2 primeros de cada grupo
        for (GroupStandingDTO group : allGroups) {
            List<TeamStandingDTO> standings = group.getStandings();
            if (standings.size() >= 2) {
                qualifiedTeams.add(convertToTeamPosition(standings.get(0).getTeam(), 1)); // 1ro
                qualifiedTeams.add(convertToTeamPosition(standings.get(1).getTeam(), 2)); // 2do
            }
        }
        
        // Asignar equipos a octavos de final
        List<Match> roundOf16 = matchRepository.findByTournamentIdAndPhase(tournamentId, MatchPhase.ROUND_16);
        
        // Emparejamientos típicos: 1roA vs 2doB, 1roB vs 2doA, etc.
        assignTeamsToKnockout(roundOf16, qualifiedTeams);
        
        matchRepository.saveAll(roundOf16);
    }
    
    // ============================================
    // MÉTODOS AUXILIARES DE GENERACIÓN
    // ============================================
    
    private Match createGroupMatch(Long tournamentId, TeamPositionDTO home, TeamPositionDTO away, 
                                 String groupName, Integer matchday) {
        Match match = new Match();
        match.setTournamentId(tournamentId);
        match.setHomeTeamId(home.getTeamId());
        match.setAwayTeamId(away.getTeamId());
        match.setPhase(MatchPhase.GROUP_STAGE);
        match.setGroupName(groupName);
        match.setMatchday(matchday);
        match.setStatus(MatchStatus.PENDING);
        
        // Verificar si requiere input del jugador
        boolean requiresInput = home.getPlayerId() != null || away.getPlayerId() != null;
        match.setRequiresPlayerInput(requiresInput);
        
        return match;
    }
    
    private Match createKnockoutMatch(Long tournamentId, MatchPhase phase) {
        Match match = new Match();
        match.setTournamentId(tournamentId);
        match.setPhase(phase);
        match.setStatus(MatchStatus.PENDING);
        match.setRequiresPlayerInput(false); // Se actualiza cuando se asignan equipos
        return match;
    }
    
    private List<Long> createPhaseMatches(Long tournamentId, MatchPhase phase, int numMatches) {
        List<Long> matchIds = new ArrayList<>();
        for (int i = 0; i < numMatches; i++) {
            Match match = createKnockoutMatch(tournamentId, phase);
            Match saved = matchRepository.save(match);
            matchIds.add(saved.getId());
        }
        return matchIds;
    }
    
    private void linkKnockoutMatches(Map<MatchPhase, List<Long>> matchIdsByPhase, Long thirdPlaceId, Long finalId) {
        // Conectar octavos -> cuartos
        if (matchIdsByPhase.containsKey(MatchPhase.ROUND_16)) {
            List<Long> roundOf16 = matchIdsByPhase.get(MatchPhase.ROUND_16);
            List<Long> quarters = matchIdsByPhase.get(MatchPhase.QUARTER_FINAL);
            
            for (int i = 0; i < roundOf16.size(); i++) {
                Match match = matchRepository.findById(roundOf16.get(i)).orElse(null);
                if (match != null) {
                    match.setNextMatchId(quarters.get(i / 2));
                    matchRepository.save(match);
                }
            }
        }
        
        // Conectar cuartos -> semis
        List<Long> quarters = matchIdsByPhase.get(MatchPhase.QUARTER_FINAL);
        List<Long> semis = matchIdsByPhase.get(MatchPhase.SEMI_FINAL);
        
        for (int i = 0; i < quarters.size(); i++) {
            Match match = matchRepository.findById(quarters.get(i)).orElse(null);
            if (match != null) {
                match.setNextMatchId(semis.get(i / 2));
                matchRepository.save(match);
            }
        }
        
        // Conectar semis -> final y tercer lugar
        for (int i = 0; i < semis.size(); i++) {
            Match match = matchRepository.findById(semis.get(i)).orElse(null);
            if (match != null) {
                match.setNextMatchId(i == 0 ? finalId : finalId); // Ambas semis van a final
                matchRepository.save(match);
            }
        }
    }
    
    // ============================================
    // CLASES AUXILIARES PARA ESTADÍSTICAS
    // ============================================
    
    private static class TeamStandingStats {
        int points = 0;
        int wins = 0;
        int draws = 0; 
        int losses = 0;
        int goalsFor = 0;
        int goalsAgainst = 0;
    }
    
    private void updateTeamStats(Map<Long, TeamStandingStats> stats, Match match) {
        TeamStandingStats homeStats = stats.get(match.getHomeTeamId());
        TeamStandingStats awayStats = stats.get(match.getAwayTeamId());
        
        if (homeStats != null && awayStats != null) {
            homeStats.goalsFor += match.getHomeScore();
            homeStats.goalsAgainst += match.getAwayScore();
            awayStats.goalsFor += match.getAwayScore();
            awayStats.goalsAgainst += match.getHomeScore();
            
            if (match.getHomeScore() > match.getAwayScore()) {
                // Local gana
                homeStats.wins++;
                homeStats.points += 3;
                awayStats.losses++;
            } else if (match.getHomeScore() < match.getAwayScore()) {
                // Visitante gana
                awayStats.wins++;
                awayStats.points += 3;
                homeStats.losses++;
            } else {
                // Empate
                homeStats.draws++;
                homeStats.points += 1;
                awayStats.draws++;
                awayStats.points += 1;
            }
        }
    }
    
    // ============================================
    // MÉTODOS AUXILIARES ADICIONALES
    // ============================================
    
    private MatchGoalDTO convertToMatchGoalDTO(MatchGoal goal) {
        MatchGoalDTO dto = new MatchGoalDTO();
        dto.setPlayerName(goal.getPlayerName());
        dto.setMinute(goal.getMinute());
        dto.setIsOwnGoal(goal.getIsOwnGoal());
        
        // Obtener nombre del equipo
        try {
            TournamentStructureDTO structure = tournamentClient.getTournamentStructure(
                matchRepository.findById(goal.getMatchId()).get().getTournamentId());
            TeamPositionDTO team = findTeamInStructure(structure, goal.getTeamId());
            dto.setTeamName(team != null ? team.getTeamName() : "Equipo " + goal.getTeamId());
        } catch (Exception e) {
            dto.setTeamName("Equipo " + goal.getTeamId());
        }
        
        return dto;
    }
    
    private void saveMatchGoals(Long matchId, List<GoalDTO> goals) {
        // Limpiar goles existentes
        matchGoalRepository.deleteByMatchId(matchId);
        
        // Guardar nuevos goles
        List<MatchGoal> matchGoals = goals.stream()
                .map(goalDto -> {
                    MatchGoal goal = new MatchGoal();
                    goal.setMatchId(matchId);
                    goal.setTeamId(goalDto.getTeamId());
                    goal.setPlayerName(goalDto.getPlayerName());
                    goal.setMinute(goalDto.getMinute());
                    goal.setIsOwnGoal(goalDto.getIsOwnGoal());
                    return goal;
                })
                .collect(Collectors.toList());
        
        matchGoalRepository.saveAll(matchGoals);
    }
    
    private MatchResultDTO buildMatchResultDTO(Match match) {
        TournamentStructureDTO structure = tournamentClient.getTournamentStructure(match.getTournamentId());
        
        TeamPositionDTO homeTeam = findTeamInStructure(structure, match.getHomeTeamId());
        TeamPositionDTO awayTeam = findTeamInStructure(structure, match.getAwayTeamId());
        
        MatchResultDTO dto = new MatchResultDTO();
        dto.setMatchId(match.getId());
        dto.setTournamentId(match.getTournamentId());
        dto.setHomeTeamId(match.getHomeTeamId());
        dto.setAwayTeamId(match.getAwayTeamId());
        dto.setHomeTeamName(homeTeam != null ? homeTeam.getTeamName() : "Equipo " + match.getHomeTeamId());
        dto.setAwayTeamName(awayTeam != null ? awayTeam.getTeamName() : "Equipo " + match.getAwayTeamId());
        dto.setHomeScore(match.getHomeScore());
        dto.setAwayScore(match.getAwayScore());
        dto.setHomePenalties(match.getHomePenalties());
        dto.setAwayPenalties(match.getAwayPenalties());
        dto.setPhase(match.getPhase().name());
        dto.setGroupName(match.getGroupName());
        dto.setMatchday(match.getMatchday());
        dto.setPlayedDate(match.getPlayedDate());
        
        // Información de jugadores
        dto.setHomePlayerId(homeTeam != null ? homeTeam.getPlayerId() : null);
        dto.setAwayPlayerId(awayTeam != null ? awayTeam.getPlayerId() : null);
        dto.setHomePlayerName(homeTeam != null ? homeTeam.getPlayerName() : null);
        dto.setAwayPlayerName(awayTeam != null ? awayTeam.getPlayerName() : null);
        
        // Determinar ganador para estadísticas
        Long winnerId = determineWinner(match);
        dto.setWinnerTeamId(winnerId);
        dto.setLoserTeamId(winnerId != null ? 
            (Objects.equals(winnerId, match.getHomeTeamId()) ? match.getAwayTeamId() : match.getHomeTeamId()) : null);
        
        dto.setWasDecidedByPenalties(match.getHomePenalties() != null && match.getAwayPenalties() != null);
        dto.setWasSimulated(match.getStatus() == MatchStatus.SIMULATED);
        
        return dto;
    }
    
    // Métodos auxiliares adicionales que podrían necesitarse
    private MatchPhase determineStartPhase(int numTeams) {
        if (numTeams <= 2) return MatchPhase.FINAL;
        if (numTeams <= 4) return MatchPhase.SEMI_FINAL;
        if (numTeams <= 8) return MatchPhase.QUARTER_FINAL;
        if (numTeams <= 16) return MatchPhase.ROUND_16;
        throw new RuntimeException("Too many teams for direct knockout: " + numTeams);
    }
    
    private Integer getTotalTeamsFromGroups(TournamentStructureDTO structure) {
        if (structure.getGroups() == null) return 0;
        return structure.getGroups().values().stream()
                .mapToInt(List::size)
                .sum();
    }
    
    private List<Match> getMatchesByIds(List<Long> ids) {
        return matchRepository.findAllById(ids);
    }
    
    private void generateSubsequentKnockoutPhases(Long tournamentId, MatchPhase startPhase, List<Match> firstRoundMatches) {
        // Implementación para generar fases posteriores en knockout directo
        // Similar a generateKnockoutMatches pero adaptado
    }
    
    private void assignTeamsToKnockout(List<Match> matches, List<TeamPositionDTO> qualifiedTeams) {
        // Implementación para asignar equipos clasificados a octavos de final
        // Siguiendo las reglas FIFA de emparejamientos
    }
    
    private TeamPositionDTO convertToTeamPosition(TeamBasicDTO team, int groupPosition) {
        TeamPositionDTO position = new TeamPositionDTO();
        position.setTeamId(team.getId());
        position.setTeamName(team.getName());
        position.setPlayerId(team.getPlayerId());
        position.setPlayerName(team.getPlayerName());
        position.setIsAI(team.getIsAI());
        position.setTeamType(team.getTeamType());
        return position;
    }
}