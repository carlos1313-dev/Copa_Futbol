package com.pes.copa.tournaments.service;

import com.pes.copa.tournaments.client.TeamsServiceClient;
import com.pes.copa.tournaments.dto.external.CountryDTO;
import com.pes.copa.tournaments.dto.external.TeamDTO;
import com.pes.copa.tournaments.dto.request.AssignTeamDTO;
import com.pes.copa.tournaments.dto.request.FillRandomTeamsDTO;
import com.pes.copa.tournaments.dto.response.AvailableTeamDTO;
import com.pes.copa.tournaments.dto.response.TeamPositionDTO;
import com.pes.copa.tournaments.dto.response.TournamentStructureDTO;
import com.pes.copa.tournaments.entity.Tournament;
import com.pes.copa.tournaments.entity.TournamentTeam;
import com.pes.copa.tournaments.enums.TeamType;
import com.pes.copa.tournaments.enums.TournamentType;
import com.pes.copa.tournaments.repository.TournamentRepository;
import com.pes.copa.tournaments.repository.TournamentTeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar las operaciones relacionadas con equipos en torneos
 * @author sangr
 */
@Slf4j
@Service
public class TournamentTeamsService {
    
    private final TournamentTeamRepository tournamentTeamRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentService tournamentService;
    private final TeamsServiceClient teamsServiceClient;
//    private final TournamentValidationService validationService;
    
    @Autowired
    public TournamentTeamsService(TournamentTeamRepository tournamentTeamRepository,
                               TournamentRepository tournamentRepository,
                               TournamentService tournamentService,
                               TeamsServiceClient teamsServiceClient
                               ) {
        this.tournamentTeamRepository = tournamentTeamRepository;
        this.tournamentRepository = tournamentRepository;
        this.tournamentService = tournamentService;
        this.teamsServiceClient = teamsServiceClient;
        
    }
    
    /**
     * Asigna un equipo a una posición específica en el torneo
     * @param tournamentId ID del torneo
     * @param dto datos de asignación del equipo
     * @return equipo asignado con información completa
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws RuntimeException si hay problemas de acceso a datos o validación
     */
    @Transactional
    public TeamPositionDTO assignTeamToPosition(Long tournamentId, AssignTeamDTO dto) {
        if (tournamentId == null || dto == null) {
            throw new IllegalArgumentException("El ID del torneo y los datos de asignación no pueden ser nulos");
        }
        
        if (dto.getTeamId() == null) {
            throw new IllegalArgumentException("El ID del equipo no puede ser nulo");
        }
        
        try {
            // Verificar que el torneo existe
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + tournamentId));
            
            // Verificar que el equipo no esté ya en el torneo
            Optional<TournamentTeam> existingTeam = tournamentTeamRepository
                    .findByTournamentIdAndTeamId(tournamentId, dto.getTeamId());
            if (existingTeam.isPresent()) {
                throw new RuntimeException("El equipo ya está asignado a este torneo");
            }
            
            // Obtener información del equipo desde Teams Service
            TeamType teamType = determineTeamType(tournament);
            Object teamInfo = getTeamInfo(dto.getTeamId(), teamType);
            
            if (teamInfo == null) {
                throw new RuntimeException("Equipo no encontrado en Teams Service con ID: " + dto.getTeamId());
            }
            
            // Crear nueva asignación
            TournamentTeam tournamentTeam = new TournamentTeam();
            tournamentTeam.setTournamentId(tournamentId);
            tournamentTeam.setTeamId(dto.getTeamId());
            tournamentTeam.setPlayerId(dto.getPlayerId());
            tournamentTeam.setGroupName(dto.getGroupName());
            tournamentTeam.setPosition(dto.getPosition());
            tournamentTeam.setIsEliminated(false);
            tournamentTeam.setTeamType(teamType);
            
            TournamentTeam saved = tournamentTeamRepository.save(tournamentTeam);
            
            // Actualizar contador de jugadores en el torneo
            tournamentService.updateCurrentPlayers(tournamentId);
            
            // Convertir a DTO con información completa
            return buildTeamPositionDTO(saved, teamInfo, null);
            
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al asignar equipo al torneo en la base de datos", e);
        }
    }
    
    /**
     * Obtiene todos los equipos disponibles para un tipo de torneo específico
     * @param tournamentId ID del torneo
     * @return lista de equipos disponibles con estado de selección
     */
    public List<AvailableTeamDTO> getAvailableTeams(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El ID del torneo no puede ser nulo");
        }
        
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + tournamentId));
            
            // Obtener equipos ya seleccionados
            List<TournamentTeam> selectedTeams = tournamentTeamRepository.findByTournamentId(tournamentId);
            Set<Long> selectedTeamIds = selectedTeams.stream()
                    .map(TournamentTeam::getTeamId)
                    .collect(Collectors.toSet());
            
            // Obtener equipos disponibles según el tipo de torneo
            List<AvailableTeamDTO> availableTeams = new ArrayList<>();
            TeamType teamType = determineTeamType(tournament);
            
            if (teamType == TeamType.COUNTRY) {
                List<CountryDTO> countries = getCountriesForTournament(tournament.getTournamentType());
                availableTeams = countries.stream()
                        .map(country -> AvailableTeamDTO.builder()
                                .id(country.getId())
                                .name(country.getName())
                                .logoURL(country.getFlagURL())
                                .continent(country.getContinent())
                                .isAlreadySelected(selectedTeamIds.contains(country.getId()))
                                .teamType("COUNTRY")
                                .build())
                        .collect(Collectors.toList());
            } else {
                List<TeamDTO> teams = getTeamsForTournament(tournament.getTournamentType());
                availableTeams = teams.stream()
                        .map(team -> AvailableTeamDTO.builder()
                                .id(team.getId())
                                .name(team.getName())
                                .logoURL(team.getLogoURL())
                                .country(team.getCountry())
                                .continent(team.getContinent())
                                .isAlreadySelected(selectedTeamIds.contains(team.getId()))
                                .teamType("CLUB")
                                .build())
                        .collect(Collectors.toList());
            }
            
            log.info("Obtenidos {} equipos disponibles para torneo {}", availableTeams.size(), tournamentId);
            return availableTeams;
            
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    /**
     * Obtiene la estructura completa del torneo con equipos asignados
     * @param tournamentId ID del torneo
     * @return estructura del torneo con posiciones y equipos
     */
    public TournamentStructureDTO getTournamentStructure(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El ID del torneo no puede ser nulo");
        }
        
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + tournamentId));
            
            List<TournamentTeam> tournamentTeams = tournamentTeamRepository
                    .findByTournamentIdOrderByGroupNameAscPositionAsc(tournamentId);
            
            // Convertir a DTOs con información completa
            List<TeamPositionDTO> teamPositions = new ArrayList<>();
            for (TournamentTeam tournamentTeam : tournamentTeams) {
                Object teamInfo = getTeamInfo(tournamentTeam.getTeamId(), tournamentTeam.getTeamType());
                TeamPositionDTO dto = buildTeamPositionDTO(tournamentTeam, teamInfo, null);
                teamPositions.add(dto);
            }
            
            // Agregar posiciones vacías según el formato del torneo
            teamPositions = fillEmptyPositions(teamPositions, tournament);
            
            // Organizar según el formato
            Map<String, List<TeamPositionDTO>> groups = null;
            List<TeamPositionDTO> bracket = null;
            
            switch (tournament.getTournamentFormat()) {
                case GROUPS_THEN_KNOCKOUT:
                    groups = organizeIntoGroups(teamPositions);
                    break;
                case DIRECT_KNOCKOUT:
                    bracket = teamPositions;
                    break;
            }
            
            return TournamentStructureDTO.builder()
                    .tournamentId(tournamentId)
                    .tournamentName(tournament.getName())
                    .format(tournament.getTournamentFormat().name())
                    .groups(groups)
                    .bracket(bracket)
                    .totalPositions(tournament.getNumTeams())
                    .filledPositions(tournamentTeams.size())
                    .isComplete(tournamentTeams.size() == tournament.getNumTeams())
                    .build();
                    
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    /**
     * Llena automáticamente las posiciones vacantes con equipos aleatorios
     * @param tournamentId ID del torneo
     * @param dto configuración para llenado automático
     * @return estructura actualizada del torneo
     */
    @Transactional
    public TournamentStructureDTO fillRandomTeams(Long tournamentId, FillRandomTeamsDTO dto) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El ID del torneo no puede ser nulo");
        }
        
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + tournamentId));
            
            List<TournamentTeam> currentTeams = tournamentTeamRepository.findByTournamentId(tournamentId);
            int emptySlots = tournament.getNumTeams() - currentTeams.size();
            
            if (emptySlots <= 0) {
                log.info("El torneo {} ya está completo", tournamentId);
                return getTournamentStructure(tournamentId);
            }
            
            // Obtener equipos disponibles
            Set<Long> excludeIds = new HashSet<>();
            if (dto.getExcludeTeamIds() != null) {
                excludeIds.addAll(dto.getExcludeTeamIds());
            }
            // Agregar equipos ya seleccionados
            excludeIds.addAll(currentTeams.stream().map(TournamentTeam::getTeamId).collect(Collectors.toSet()));
            
            List<Long> availableTeamIds = getRandomAvailableTeams(tournament, excludeIds, 
                    dto.getOnlyFromContinent(), emptySlots);
            
            // Asignar equipos a posiciones vacías
            List<Integer> emptyPositions = findEmptyPositions(currentTeams, tournament.getNumTeams());
            
            for (int i = 0; i < Math.min(availableTeamIds.size(), emptyPositions.size()); i++) {
                AssignTeamDTO assignDto = new AssignTeamDTO();
                assignDto.setTeamId(availableTeamIds.get(i));
                assignDto.setPosition(emptyPositions.get(i));
                assignDto.setPlayerId(dto.getIncludeAiPlayers() ? null : null); // IA por defecto
                
                assignTeamToPosition(tournamentId, assignDto);
            }
            
            log.info("Asignados {} equipos aleatorios al torneo {}", 
                    Math.min(availableTeamIds.size(), emptyPositions.size()), tournamentId);
            
            return getTournamentStructure(tournamentId);
            
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al llenar equipos aleatorios en la base de datos", e);
        }
    }
    
    /**
     * Obtiene todos los equipos de un torneo con información completa
     * @param tournamentId ID del torneo
     * @return lista de equipos del torneo con información detallada
     */
    public List<TeamPositionDTO> getTeamsByTournament(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El ID del torneo no puede ser nulo");
        }
        
        try {
            List<TournamentTeam> tournamentTeams = tournamentTeamRepository
                    .findByTournamentIdOrderByGroupNameAscPositionAsc(tournamentId);
            
            return tournamentTeams.stream()
                    .map(tournamentTeam -> {
                        Object teamInfo = getTeamInfo(tournamentTeam.getTeamId(), tournamentTeam.getTeamType());
                        return buildTeamPositionDTO(tournamentTeam, teamInfo, null);
                    })
                    .collect(Collectors.toList());
                    
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    
    
    /**
     * Obtiene equipos de un grupo específico con información completa
     * @param tournamentId ID del torneo
     * @param groupName nombre del grupo
     * @return lista de equipos del grupo con información detallada
     */
    public List<TeamPositionDTO> getTeamsByGroup(Long tournamentId, String groupName) {
        if (tournamentId == null || groupName == null) {
            throw new IllegalArgumentException("El ID del torneo y el nombre del grupo no pueden ser nulos");
        }
        
        try {
            List<TournamentTeam> tournamentTeams = tournamentTeamRepository
                    .findByTournamentIdAndGroupName(tournamentId, groupName);
            
            return tournamentTeams.stream()
                    .map(tournamentTeam -> {
                        Object teamInfo = getTeamInfo(tournamentTeam.getTeamId(), tournamentTeam.getTeamType());
                        return buildTeamPositionDTO(tournamentTeam, teamInfo, null);
                    })
                    .collect(Collectors.toList());
                    
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    /**
     * Elimina un equipo del torneo
     * @param tournamentId ID del torneo
     * @param teamId ID del equipo
     */
    @Transactional
    public void removeTeamFromTournament(Long tournamentId, Long teamId) {
        if (tournamentId == null || teamId == null) {
            throw new IllegalArgumentException("El ID del torneo y del equipo no pueden ser nulos");
        }
        
        try {
            TournamentTeam tournamentTeam = tournamentTeamRepository
                    .findByTournamentIdAndTeamId(tournamentId, teamId)
                    .orElseThrow(() -> new RuntimeException("Equipo no encontrado en este torneo"));
            
            tournamentTeamRepository.delete(tournamentTeam);
            
            // Actualizar contador de jugadores
            tournamentService.updateCurrentPlayers(tournamentId);
            
            log.info("Equipo {} removido del torneo {}", teamId, tournamentId);
            
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al eliminar equipo del torneo en la base de datos", e);
        }
    }
    
    /**
     * Obtiene equipos controlados por un jugador específico en un torneo
     * @param tournamentId ID del torneo
     * @param playerId ID del jugador
     * @return lista de equipos del jugador con información detallada
     */
    public List<TeamPositionDTO> getTeamsByPlayer(Long tournamentId, Long playerId) {
        if (tournamentId == null || playerId == null) {
            throw new IllegalArgumentException("El ID del torneo y del jugador no pueden ser nulos");
        }
        
        try {
            List<TournamentTeam> tournamentTeams = tournamentTeamRepository
                    .findByTournamentIdAndPlayerId(tournamentId, playerId);
            
            return tournamentTeams.stream()
                    .map(tournamentTeam -> {
                        Object teamInfo = getTeamInfo(tournamentTeam.getTeamId(), tournamentTeam.getTeamType());
                        return buildTeamPositionDTO(tournamentTeam, teamInfo, null);
                    })
                    .collect(Collectors.toList());
                    
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    /**
     * Obtiene equipos activos (no eliminados) con información completa
     * @param tournamentId ID del torneo
     * @return lista de equipos activos con información detallada
     */
    public List<TeamPositionDTO> getActiveTeams(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El ID del torneo no puede ser nulo");
        } 
        
        try {
            List<TournamentTeam> tournamentTeams = tournamentTeamRepository
                    .findByTournamentIdAndIsEliminatedFalse(tournamentId);
            
            return tournamentTeams.stream()
                    .map(tournamentTeam -> {
                        Object teamInfo = getTeamInfo(tournamentTeam.getTeamId(), tournamentTeam.getTeamType());
                        return buildTeamPositionDTO(tournamentTeam, teamInfo, null);
                    })
                    .collect(Collectors.toList());
                    
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    // Métodos privados auxiliares
    
    private TeamType determineTeamType(Tournament tournament) {
        switch (tournament.getTournamentType()) {
            case WORLD_CUP:
            case COPA_AMERICA:
            case EURO:
            case CUSTOM_COUNTRIES:
                return TeamType.COUNTRY;
            case CHAMPIONS_LEAGUE:
            case CUSTOM_CLUBS:
                return TeamType.CLUB;
            default:
                return TeamType.COUNTRY;
        }
    }
    
    private Object getTeamInfo(Long teamId, TeamType teamType) {
        try {
            if (teamType == TeamType.COUNTRY) {
                return teamsServiceClient.getCountryById(teamId);
            } else {
                return teamsServiceClient.getTeamById(teamId);
            }
        } catch (Exception e) {
            log.error("Error al obtener información del equipo {} de tipo {}: {}", 
                    teamId, teamType, e.getMessage());
            return null;
        }
    }
    
    private List<CountryDTO> getCountriesForTournament(TournamentType tournamentType) {
        try {
            return teamsServiceClient.getCountriesByTournamentType(tournamentType);
        } catch (Exception e) {
            log.error("Error al obtener países para torneo {}: {}", tournamentType, e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private List<TeamDTO> getTeamsForTournament(TournamentType tournamentType) {
        try {
            return teamsServiceClient.getTeamsByTournamentType(tournamentType);
        } catch (Exception e) {
            log.error("Error al obtener equipos para torneo {}: {}", tournamentType, e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private TeamPositionDTO buildTeamPositionDTO(TournamentTeam tournamentTeam, Object teamInfo, String playerName) {
        TeamPositionDTO.TeamPositionDTOBuilder builder = TeamPositionDTO.builder()
                .teamId(tournamentTeam.getTeamId())
                .playerId(tournamentTeam.getPlayerId())
                .playerName(playerName)
                .position(tournamentTeam.getPosition())
                .groupName(tournamentTeam.getGroupName())
                .isEliminated(tournamentTeam.getIsEliminated())
                .isEmpty(false)
                .isAI(tournamentTeam.getPlayerId() == null)
                .teamType(tournamentTeam.getTeamType().name());
        
        if (teamInfo instanceof CountryDTO) {
            CountryDTO country = (CountryDTO) teamInfo;
            builder.teamName(country.getName())
                   .logoURL(country.getFlagURL());
        } else if (teamInfo instanceof TeamDTO) {
            TeamDTO team = (TeamDTO) teamInfo;
            builder.teamName(team.getName())
                   .logoURL(team.getLogoURL())
                   .country(team.getCountry());
        } else {
            builder.teamName("Equipo no encontrado")
                   .logoURL("");
        }
        
        return builder.build();
    }
    
    private List<TeamPositionDTO> fillEmptyPositions(List<TeamPositionDTO> existingTeams, Tournament tournament) {
        Set<Integer> occupiedPositions = existingTeams.stream()
                .map(TeamPositionDTO::getPosition)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        List<TeamPositionDTO> result = new ArrayList<>(existingTeams);
        
        for (int i = 1; i <= tournament.getNumTeams(); i++) {
            if (!occupiedPositions.contains(i)) {
                result.add(TeamPositionDTO.builder()
                        .position(i)
                        .isEmpty(true)
                        .isAI(false)
                        .isEliminated(false)
                        .teamType("EMPTY")
                        .build());
            }
        }
        
        return result.stream()
                .sorted(Comparator.comparing(TeamPositionDTO::getPosition, 
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }
    
    private Map<String, List<TeamPositionDTO>> organizeIntoGroups(List<TeamPositionDTO> teams) {
        return teams.stream()
                .filter(team -> team.getGroupName() != null)
                .collect(Collectors.groupingBy(TeamPositionDTO::getGroupName));
    }
    
    private List<Long> getRandomAvailableTeams(Tournament tournament, Set<Long> excludeIds, 
                                             String continent, int count) {
        List<Long> availableIds = new ArrayList<>();
        
        if (determineTeamType(tournament) == TeamType.COUNTRY) {
            List<CountryDTO> countries;
            if (continent != null && !continent.trim().isEmpty()) {
                countries = teamsServiceClient.getCountriesByContinent(continent);
            } else {
                countries = getCountriesForTournament(tournament.getTournamentType());
            }
            availableIds = countries.stream()
                    .map(CountryDTO::getId)
                    .filter(id -> !excludeIds.contains(id))
                    .collect(Collectors.toList());
        } else {
            List<TeamDTO> teams;
            if (continent != null && !continent.trim().isEmpty()) {
                teams = teamsServiceClient.getTeamsByContinent(continent);
            } else {
                teams = getTeamsForTournament(tournament.getTournamentType());
            }
            availableIds = teams.stream()
                    .map(TeamDTO::getId)
                    .filter(id -> !excludeIds.contains(id))
                    .collect(Collectors.toList());
        }
        
        Collections.shuffle(availableIds);
        return availableIds.stream()
                .limit(count)
                .collect(Collectors.toList());
    }
    
    private List<Integer> findEmptyPositions(List<TournamentTeam> currentTeams, int totalTeams) {
        Set<Integer> occupiedPositions = currentTeams.stream()
                .map(TournamentTeam::getPosition)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        List<Integer> emptyPositions = new ArrayList<>();
        for (int i = 1; i <= totalTeams; i++) {
            if (!occupiedPositions.contains(i)) {
                emptyPositions.add(i);
            }
        }
        
        return emptyPositions;
    }
}