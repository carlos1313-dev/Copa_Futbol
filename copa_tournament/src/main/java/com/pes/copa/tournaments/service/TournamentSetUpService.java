package com.pes.copa.tournaments.service;

import com.pes.copa.tournaments.client.AuthServiceClient;
import java.util.List;
import java.util.Map;
import com.pes.copa.tournaments.service.TournamentService;
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
import com.pes.copa.tournaments.enums.TournamentFormat;
import com.pes.copa.tournaments.enums.TournamentStatus;
import com.pes.copa.tournaments.enums.TournamentType;
import com.pes.copa.tournaments.repository.TournamentRepository;
import com.pes.copa.tournaments.repository.TournamentTeamRepository;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para configuración y setup de torneos
 * Maneja la lógica de asignación de equipos y estructura del torneo
 * @author sangr
 */
@Service
public class TournamentSetUpService {
    
    private final TournamentRepository tournamentRepository;
    private final TournamentTeamRepository tournamentTeamRepository;
    private final TournamentTeamsService tournamentTeamsService;
    private final TeamsServiceClient teamsServiceClient;
//    private final AuthServiceClient authServiceClient;
    
    @Autowired
    public TournamentSetUpService(TournamentRepository tournamentRepository,
                                TournamentTeamRepository tournamentTeamRepository,
                                TournamentTeamsService tournamentTeamsService,
                                TeamsServiceClient teamsServiceClient
                                ) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentTeamRepository = tournamentTeamRepository;
        this.tournamentTeamsService = tournamentTeamsService;
        this.teamsServiceClient = teamsServiceClient;
//        this.authServiceClient = authServiceClient;
    }
    
    /**
     * Obtiene equipos disponibles para un torneo específico
     * @param tournamentId ID del torneo
     * @return lista de equipos que pueden participar
     * @throws IllegalArgumentException si el tournamentId es nulo
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public List<AvailableTeamDTO> getAvailableTeams(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El ID del torneo no puede ser nulo");
        }
        
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + tournamentId));
            
            // Obtener equipos ya asignados al torneo
            List<TournamentTeam> assignedTeams = tournamentTeamRepository.findByTournamentId(tournamentId);
            Set<Long> assignedTeamIds = assignedTeams.stream()
                    .map(TournamentTeam::getTeamId)
                    .collect(Collectors.toSet());
            
            // Obtener equipos del Teams Service basado en el tipo de torneo
            List<AvailableTeamDTO> availableTeams = getTeamsFromExternalService(tournament.getTournamentType());
            
            // Marcar cuáles ya están seleccionados
            return availableTeams.stream()
                    .map(team -> {
                        team.setIsAlreadySelected(assignedTeamIds.contains(team.getId()));
                        return team;
                    })
                    .collect(Collectors.toList());
                    
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    /**
     * Obtiene la estructura actual del torneo (grupos/bracket con posiciones)
     * @param tournamentId ID del torneo
     * @return estructura completa del torneo
     * @throws IllegalArgumentException si el tournamentId es nulo
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public TournamentStructureDTO getTournamentStructure(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El ID del torneo no puede ser nulo");
        }
        
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + tournamentId));
            
            List<TournamentTeam> assignedTeams = tournamentTeamRepository
                    .findByTournamentIdOrderByGroupNameAscPositionAsc(tournamentId);
            
            return buildTournamentStructure(tournament, assignedTeams);
            
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    /**
     * Asigna un equipo a una posición específica en el torneo
     * @param tournamentId ID del torneo
     * @param dto datos de la asignación
     * @return estructura actualizada del torneo
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws RuntimeException si hay conflictos o problemas de BD
     */
    @Transactional
    public TournamentStructureDTO assignTeamToPosition(Long tournamentId, AssignTeamDTO dto) {
        if (tournamentId == null || dto == null) {
            throw new IllegalArgumentException("El ID del torneo y los datos de asignación no pueden ser nulos");
        }
        
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + tournamentId));
            
            // Verificar que el torneo esté en estado correcto para modificaciones
            if (tournament.getStatus() != TournamentStatus.CREATED && 
                tournament.getStatus() != TournamentStatus.WAITING_PLAYERS) {
                throw new RuntimeException("No se pueden modificar equipos en un torneo en progreso o finalizado");
            }
            
            // Verificar que la posición no esté ocupada
            if (isPositionOccupied(tournamentId, dto.getGroupName(), dto.getPosition())) {
                throw new RuntimeException("La posición ya está ocupada");
            }
            
            // Asignar el equipo usando el service existente
            tournamentTeamsService.assignTeamToPosition(tournamentId, dto);
            
            // Retornar estructura actualizada
            return getTournamentStructure(tournamentId);
            
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al asignar equipo al torneo", e);
        }
    }
    
    /**
     * Llena automáticamente las posiciones vacantes con equipos aleatorios
     * @param tournamentId ID del torneo
     * @param dto configuración para el llenado automático
     * @return estructura actualizada del torneo
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    @Transactional
    public TournamentStructureDTO fillRandomTeams(Long tournamentId, FillRandomTeamsDTO dto) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El ID del torneo no puede ser nulo");
        }
        
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + tournamentId));
            
            // Obtener equipos disponibles
            List<AvailableTeamDTO> availableTeams = getAvailableTeams(tournamentId).stream()
                    .filter(team -> !team.getIsAlreadySelected())
                    .collect(Collectors.toList());
            
            // Aplicar filtros si los hay
            if (dto != null) {
                if (dto.getExcludeTeamIds() != null && !dto.getExcludeTeamIds().isEmpty()) {
                    availableTeams = availableTeams.stream()
                            .filter(team -> !dto.getExcludeTeamIds().contains(team.getId()))
                            .collect(Collectors.toList());
                }
                
                if (dto.getOnlyFromContinent() != null && !dto.getOnlyFromContinent().trim().isEmpty()) {
                    availableTeams = availableTeams.stream()
                            .filter(team -> dto.getOnlyFromContinent().equalsIgnoreCase(team.getContinent()))
                            .collect(Collectors.toList());
                }
            }
            
            // Obtener posiciones vacías
            List<EmptyPosition> emptyPositions = getEmptyPositions(tournament);
            
            // Mezclar equipos disponibles aleatoriamente
            Collections.shuffle(availableTeams);
            
            // Asignar equipos a posiciones vacías
            int teamsToAssign = Math.min(availableTeams.size(), emptyPositions.size());
            boolean includeAI = dto == null || dto.getIncludeAiPlayers() == null || dto.getIncludeAiPlayers();
            
            for (int i = 0; i < teamsToAssign; i++) {
                AvailableTeamDTO team = availableTeams.get(i);
                EmptyPosition position = emptyPositions.get(i);
                
                AssignTeamDTO assignDto = new AssignTeamDTO();
                assignDto.setTeamId(team.getId());
                assignDto.setPlayerId(includeAI ? null : null); // null = IA
                assignDto.setGroupName(position.getGroupName());
                assignDto.setPosition(position.getPosition());
                
                tournamentTeamsService.assignTeamToPosition(tournamentId, assignDto);
            }
            
            return getTournamentStructure(tournamentId);
            
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al llenar equipos aleatorios", e);
        }
    }
    
    /**
     * Reinicia completamente el setup del torneo
     * @param tournamentId ID del torneo
     * @return estructura vacía del torneo
     * @throws IllegalArgumentException si el tournamentId es nulo
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    @Transactional
    public TournamentStructureDTO resetTournamentSetup(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El ID del torneo no puede ser nulo");
        }
        
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + tournamentId));
            
            // Eliminar todos los equipos del torneo
            tournamentTeamRepository.deleteByTournamentId(tournamentId);
            
            // Resetear estado si es necesario
            if (tournament.getStatus() == TournamentStatus.WAITING_PLAYERS) {
                tournament.setStatus(TournamentStatus.CREATED);
                tournament.setNumPlayers(0);
                tournamentRepository.save(tournament);
            }
            
            return getTournamentStructure(tournamentId);
            
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al resetear el torneo", e);
        }
    }
    
    /**
     * Finaliza el setup y prepara el torneo para comenzar
     * @param tournamentId ID del torneo
     * @return estructura finalizada del torneo
     * @throws IllegalArgumentException si el tournamentId es nulo
     * @throws RuntimeException si el setup no está completo
     */
    @Transactional
    public TournamentStructureDTO finalizeTournamentSetup(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El ID del torneo no puede ser nulo");
        }
        
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + tournamentId));
            
            // Verificar que el setup esté completo
            long assignedTeams = tournamentTeamRepository.countByTournamentId(tournamentId);
            if (assignedTeams != tournament.getNumTeams()) {
                throw new RuntimeException("El torneo no está completo. Faltan " + 
                        (tournament.getNumTeams() - assignedTeams) + " equipos");
            }
            
            // Cambiar estado a IN_PROGRESS
            tournament.setStatus(TournamentStatus.IN_PROGRESS);
            tournamentRepository.save(tournament);
            
            return getTournamentStructure(tournamentId);
            
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al finalizar el setup del torneo", e);
        }
    }
    
    /**
     * Construye la estructura del torneo con equipos en sus posiciones
     * @param tournament el torneo
     * @param assignedTeams equipos ya asignados
     * @return estructura completa del torneo
     */
    private TournamentStructureDTO buildTournamentStructure(Tournament tournament, List<TournamentTeam> assignedTeams) {
        TournamentStructureDTO.TournamentStructureDTOBuilder builder = TournamentStructureDTO.builder()
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .format(tournament.getTournamentFormat().name())
                .totalPositions(tournament.getNumTeams())
                .filledPositions(assignedTeams.size())
                .isComplete(assignedTeams.size() == tournament.getNumTeams());
        
        if (tournament.getTournamentFormat() == TournamentFormat.GROUPS_THEN_KNOCKOUT) {
            // Estructura con grupos
            Map<String, List<TeamPositionDTO>> groups = buildGroupsStructure(tournament, assignedTeams);
            builder.groups(groups);
        } else {
            // Estructura de eliminación directa
            List<TeamPositionDTO> bracket = buildBracketStructure(tournament, assignedTeams);
            builder.bracket(bracket);
        }
        
        return builder.build();
    }
    
    /**
     * Construye la estructura de grupos
     * @param tournament el torneo
     * @param assignedTeams equipos asignados
     * @return mapa de grupos con sus equipos
     */
    private Map<String, List<TeamPositionDTO>> buildGroupsStructure(Tournament tournament, List<TournamentTeam> assignedTeams) {
        Map<String, List<TeamPositionDTO>> groups = new LinkedHashMap<>();
        
        int numGroups = tournament.getNumTeams() / 4; // 4 equipos por grupo
        
        // Crear grupos A, B, C, etc.
        for (int i = 0; i < numGroups; i++) {
            String groupName = String.valueOf((char) ('A' + i));
            List<TeamPositionDTO> groupTeams = new ArrayList<>();
            
            // Crear 4 posiciones por grupo
            for (int pos = 1; pos <= 4; pos++) {
                TeamPositionDTO teamPosition = findTeamInPosition(assignedTeams, groupName, pos);
                if (teamPosition == null) {
                    // Crear posición vacía
                    teamPosition = TeamPositionDTO.builder()
                            .groupName(groupName)
                            .position(pos)
                            .isEmpty(true)
                            .isAI(false)
                            .isEliminated(false)
                            .build();
                }
                groupTeams.add(teamPosition);
            }
            
            groups.put(groupName, groupTeams);
        }
        
        return groups;
    }
    
    /**
     * Construye la estructura de bracket para eliminación directa
     * @param tournament el torneo
     * @param assignedTeams equipos asignados
     * @return lista de posiciones en el bracket
     */
    private List<TeamPositionDTO> buildBracketStructure(Tournament tournament, List<TournamentTeam> assignedTeams) {
        List<TeamPositionDTO> bracket = new ArrayList<>();
        
        // Crear todas las posiciones del bracket
        for (int pos = 1; pos <= tournament.getNumTeams(); pos++) {
            TeamPositionDTO teamPosition = findTeamInPosition(assignedTeams, null, pos);
            if (teamPosition == null) {
                // Crear posición vacía
                teamPosition = TeamPositionDTO.builder()
                        .position(pos)
                        .isEmpty(true)
                        .isAI(false)
                        .isEliminated(false)
                        .build();
            }
            bracket.add(teamPosition);
        }
        
        return bracket;
    }
    
    /**
     * Busca un equipo en una posición específica
     * @param assignedTeams lista de equipos asignados
     * @param groupName nombre del grupo (puede ser null para bracket)
     * @param position posición
     * @return equipo en esa posición o null si está vacía
     */
    private TeamPositionDTO findTeamInPosition(List<TournamentTeam> assignedTeams, String groupName, Integer position) {
        Optional<TournamentTeam> teamInPosition = assignedTeams.stream()
                .filter(team -> Objects.equals(team.getGroupName(), groupName) && 
                               Objects.equals(team.getPosition(), position))
                .findFirst();
        
        if (teamInPosition.isPresent()) {
            TournamentTeam team = teamInPosition.get();
            
            // Obtener datos del equipo desde Teams Service
            String teamName = "Equipo " + team.getTeamId(); // Fallback
            String logoURL = "";
            String country = "";
            String playerName = null;
            
            try {
                if (team.getTeamType() == TeamType.COUNTRY) {
                    CountryDTO countryData = teamsServiceClient.getCountryById(team.getTeamId());
                    if (countryData != null) {
                        teamName = countryData.getName();
                        logoURL = countryData.getFlagURL();
                        country = null; // No aplica para países
                    }
                } else {
                    TeamDTO teamData = teamsServiceClient.getTeamById(team.getTeamId());
                    if (teamData != null) {
                        teamName = teamData.getName();
                        logoURL = teamData.getLogoURL();
                        country = teamData.getCountry();
                    }
                }
                
                // Obtener información del jugador si existe
//                if (team.getPlayerId() != null) {
//                    UserDTO userData = authServiceClient.getUserById(team.getPlayerId());
//                    if (userData != null) {
//                        playerName = userData.getDisplayName() != null ? 
//                                   userData.getDisplayName() : userData.getUsername();
//                    }
//                }
                
            } catch (Exception e) {
                // En caso de error, usar valores por defecto
                // Log del error pero no fallar
                System.err.println("Error obteniendo datos del equipo " + team.getTeamId() + ": " + e.getMessage());
            }
            
            return TeamPositionDTO.builder()
                    .teamId(team.getTeamId())
                    .teamName(teamName)
                    .logoURL(logoURL)
                    .country(country)
                    .playerId(team.getPlayerId())
                    .playerName(playerName)
                    .position(team.getPosition())
                    .groupName(team.getGroupName())
                    .isEliminated(team.getIsEliminated())
                    .isEmpty(false)
                    .isAI(team.getPlayerId() == null)
                    .teamType(team.getTeamType().name())
                    .build();
        }
        
        return null;
    }
    
    /**
     * Verifica si una posición específica está ocupada
     * @param tournamentId ID del torneo
     * @param groupName nombre del grupo (puede ser null)
     * @param position posición
     * @return true si está ocupada
     */
    private boolean isPositionOccupied(Long tournamentId, String groupName, Integer position) {
        if (position == null) {
            return false;
        }
        
        try {
            return tournamentTeamRepository.existsByTournamentIdAndGroupNameAndPosition(
                    tournamentId, groupName, position);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al verificar posición", e);
        }
    }
    
    /**
     * Obtiene las posiciones vacías del torneo
     * @param tournament el torneo
     * @return lista de posiciones vacías
     */
    private List<EmptyPosition> getEmptyPositions(Tournament tournament) {
        List<EmptyPosition> emptyPositions = new ArrayList<>();
        List<TournamentTeam> assignedTeams = tournamentTeamRepository.findByTournamentId(tournament.getId());
        
        if (tournament.getTournamentFormat() == TournamentFormat.GROUPS_THEN_KNOCKOUT) {
            // Obtener posiciones vacías en grupos
            int numGroups = tournament.getNumTeams() / 4;
            for (int i = 0; i < numGroups; i++) {
                String groupName = String.valueOf((char) ('A' + i));
                for (int pos = 1; pos <= 4; pos++) {
                    if (!isPositionTaken(assignedTeams, groupName, pos)) {
                        emptyPositions.add(new EmptyPosition(groupName, pos));
                    }
                }
            }
        } else {
            // Obtener posiciones vacías en bracket
            for (int pos = 1; pos <= tournament.getNumTeams(); pos++) {
                if (!isPositionTaken(assignedTeams, null, pos)) {
                    emptyPositions.add(new EmptyPosition(null, pos));
                }
            }
        }
        
        return emptyPositions;
    }
    
    /**
     * Verifica si una posición está tomada en la lista de equipos asignados
     */
    private boolean isPositionTaken(List<TournamentTeam> assignedTeams, String groupName, Integer position) {
        return assignedTeams.stream()
                .anyMatch(team -> Objects.equals(team.getGroupName(), groupName) && 
                                 Objects.equals(team.getPosition(), position));
    }
    
    /**
     * Obtiene equipos del servicio externo
     * @param tournamentType tipo de torneo
     * @return lista de equipos disponibles
     */
    private List<AvailableTeamDTO> getTeamsFromExternalService(TournamentType tournamentType) {
        try {
            TeamType expectedTeamType = determineTeamTypeFromTournament(tournamentType);
            
            if (expectedTeamType == TeamType.COUNTRY) {
                // Obtener países/selecciones
                List<CountryDTO> countries = teamsServiceClient.getCountriesByTournamentType(tournamentType);
                return countries.stream()
                        .map(country -> AvailableTeamDTO.builder()
                                .id(country.getId())
                                .name(country.getName())
                                .logoURL(country.getFlagURL())
                                .country(null) // No aplica para países
                                .continent(country.getContinent())
                                .isAlreadySelected(false)
                                .teamType(TeamType.COUNTRY.name())
                                .build())
                        .collect(Collectors.toList());
            } else {
                // Obtener equipos/clubes
                List<TeamDTO> teams = teamsServiceClient.getTeamsByTournamentType(tournamentType);
                return teams.stream()
                        .map(team -> AvailableTeamDTO.builder()
                                .id(team.getId())
                                .name(team.getName())
                                .logoURL(team.getLogoURL())
                                .country(team.getCountry())
                                .continent(team.getContinent())
                                .isAlreadySelected(false)
                                .teamType(TeamType.CLUB.name())
                                .build())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            // En caso de error, devolver lista vacía y loggear
            // En producción podrías tener un fallback con datos mock
            throw new RuntimeException("Error al obtener equipos del Teams Service: " + e.getMessage(), e);
        }
    }
    
    /**
     * Determina el tipo de equipo basado en el tipo de torneo
     */
    private TeamType determineTeamTypeFromTournament(TournamentType tournamentType) {
        switch (tournamentType) {
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
    
    /**
     * Clase interna para representar posiciones vacías
     */
    private static class EmptyPosition {
        private final String groupName;
        private final Integer position;
        
        public EmptyPosition(String groupName, Integer position) {
            this.groupName = groupName;
            this.position = position;
        }
        
        public String getGroupName() { return groupName; }
        public Integer getPosition() { return position; }
    }
}