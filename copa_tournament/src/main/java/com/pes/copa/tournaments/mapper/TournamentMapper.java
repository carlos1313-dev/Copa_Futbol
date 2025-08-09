package com.pes.copa.tournaments.mapper;

import com.pes.copa.tournaments.dto.external.TeamDTO;
import com.pes.copa.tournaments.dto.external.CountryDTO;
import com.pes.copa.tournaments.dto.external.UserDTO;
import com.pes.copa.tournaments.dto.request.CreateTournamentDTO;
import com.pes.copa.tournaments.dto.response.AvailableTeamDTO;
import com.pes.copa.tournaments.dto.response.TeamPositionDTO;
import com.pes.copa.tournaments.dto.response.TournamentDTO;
import com.pes.copa.tournaments.dto.response.TournamentStructureDTO;
import com.pes.copa.tournaments.entity.Tournament;
import com.pes.copa.tournaments.entity.TournamentTeam;
import com.pes.copa.tournaments.enums.TournamentStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades JPA y DTOs
 * Maneja las conversiones y enriquecimiento de datos de otros servicios
 * @author sangr
 */
@Component
public class TournamentMapper {
    
    // =====================================
    // CONVERSIONES ENTITY <-> DTO BÁSICAS
    // =====================================
    
    /**
     * Convierte CreateTournamentDTO a Entity Tournament
     * @param dto datos del frontend
     * @return entidad para guardar en BD
     */
    public Tournament dtoToEntity(CreateTournamentDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Tournament tournament = new Tournament();
        tournament.setName(dto.getName().trim());
        tournament.setTournamentType(dto.getTournamentType());
        tournament.setTournamentFormat(dto.getTournamentFormat());
        tournament.setMaxPlayers(dto.getMaxPlayers());
        tournament.setTotalTeams(dto.getTotalTeams());
        tournament.setCreatorUserId(dto.getCreatorUserId());
        
        // Valores por defecto al crear
        tournament.setStatus(TournamentStatus.CREATED);
        tournament.setCurrentPlayers(0);
        tournament.setCreatedDate(LocalDateTime.now());
        
        return tournament;
    }
    
    /**
     * Convierte Entity Tournament a TournamentDTO para respuesta
     * @param tournament entidad de BD
     * @return DTO para enviar al frontend
     */
    public TournamentDTO entityToDTO(Tournament tournament) {
        if (tournament == null) {
            return null;
        }
        
        return TournamentDTO.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .tournamentType(tournament.getTournamentType().getDisplayName())
                .tournamentFormat(tournament.getTournamentFormat().getDisplayName())
                .status(tournament.getStatus().getDisplayName())
                .maxPlayers(tournament.getMaxPlayers())
                .currentPlayers(tournament.getCurrentPlayers())
                .totalTeams(tournament.getTotalTeams())
                .createdDate(tournament.getCreatedDate())
                .creatorUserId(tournament.getCreatorUserId())
                .canJoin(calculateCanJoin(tournament))
                .isStarted(calculateIsStarted(tournament))
                .build();
    }
    
    /**
     * Convierte lista de Tournament entities a lista de TournamentDTO
     * @param tournaments lista de entidades
     * @return lista de DTOs
     */
    public List<TournamentDTO> entitiesToDTOs(List<Tournament> tournaments) {
        if (tournaments == null) {
            return new ArrayList<>();
        }
        
        return tournaments.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }
    
    // =====================================
    // MAPEOS CON DATOS EXTERNOS (Teams, Auth services)
    // =====================================
    
    /**
     * Convierte TeamDTO (del Teams service) a AvailableTeamDTO
     * @param teamDTO datos del equipo del Teams service
     * @param isAlreadySelected si ya está seleccionado en el torneo
     * @return DTO para mostrar en lista de equipos disponibles
     */
    public AvailableTeamDTO teamToAvailableTeam(TeamDTO teamDTO, boolean isAlreadySelected) {
        if (teamDTO == null) {
            return null;
        }
        
        return AvailableTeamDTO.builder()
                .id(teamDTO.getId())
                .name(teamDTO.getName())
                .logoURL(teamDTO.getLogoURL())
                .country(teamDTO.getCountry())
                .continent(teamDTO.getContinent())
                .isAlreadySelected(isAlreadySelected)
                .teamType("CLUB")
                .build();
    }
    
    /**
     * Convierte CountryDTO (del Teams service) a AvailableTeamDTO
     * @param countryDTO datos del país del Teams service
     * @param isAlreadySelected si ya está seleccionado en el torneo
     * @return DTO para mostrar en lista de selecciones disponibles
     */
    public AvailableTeamDTO countryToAvailableTeam(CountryDTO countryDTO, boolean isAlreadySelected) {
        if (countryDTO == null) {
            return null;
        }
        
        return AvailableTeamDTO.builder()
                .id(countryDTO.getId())
                .name(countryDTO.getName())
                .logoURL(countryDTO.getFlagURL())
                .country(null) // Los países no tienen "país padre"
                .continent(countryDTO.getContinent())
                .isAlreadySelected(isAlreadySelected)
                .teamType("COUNTRY")
                .build();
    }
    
    /**
     * Convierte TournamentTeam + datos externos a TeamPositionDTO
     * @param tournamentTeam entidad de la posición en el torneo
     * @param teamData datos del equipo (viene de Teams service)
     * @param userData datos del jugador (viene de Auth service, puede ser null)
     * @return DTO completo para mostrar posición en el torneo
     */
    public TeamPositionDTO tournamentTeamToPosition(TournamentTeam tournamentTeam, 
                                                  Object teamData, // TeamDTO o CountryDTO
                                                  UserDTO userData) {
        if (tournamentTeam == null) {
            return createEmptyPosition(null, null);
        }
        
        String teamName = "";
        String logoURL = "";
        String country = null;
        
        // Extraer datos según el tipo de equipo
        if (teamData instanceof TeamDTO) {
            TeamDTO team = (TeamDTO) teamData;
            teamName = team.getName();
            logoURL = team.getLogoURL();
            country = team.getCountry();
        } else if (teamData instanceof CountryDTO) {
            CountryDTO countryData = (CountryDTO) teamData;
            teamName = countryData.getName();
            logoURL = countryData.getFlagURL();
            country = null; // Los países no tienen país padre
        }
        
        return TeamPositionDTO.builder()
                .teamId(tournamentTeam.getTeamId())
                .teamName(teamName)
                .logoURL(logoURL)
                .country(country)
                .playerId(tournamentTeam.getPlayerId())
                .playerName(userData != null ? userData.getUsername() : null)
                .position(tournamentTeam.getPosition())
                .groupName(tournamentTeam.getGroupName())
                .isEliminated(tournamentTeam.getIsEliminated())
                .isEmpty(false)
                .isAI(tournamentTeam.getPlayerId() == null)
                .teamType(tournamentTeam.getTeamType().getDisplayName())
                .build();
    }
    
    /**
     * Crea un TeamPositionDTO vacío para posiciones sin equipo asignado
     * @param position posición en el torneo
     * @param groupName grupo (si aplica)
     * @return DTO de posición vacía
     */
    public TeamPositionDTO createEmptyPosition(Integer position, String groupName) {
        return TeamPositionDTO.builder()
                .teamId(null)
                .teamName(null)
                .logoURL(null)
                .country(null)
                .playerId(null)
                .playerName(null)
                .position(position)
                .groupName(groupName)
                .isEliminated(false)
                .isEmpty(true)
                .isAI(false)
                .teamType(null)
                .build();
    }
    
    // =====================================
    // CONSTRUCCIÓN DE ESTRUCTURA DE TORNEO
    // =====================================
    
    /**
     * Construye la estructura completa del torneo organizando por grupos/bracket
     * @param tournament datos básicos del torneo
     * @param positions lista de todas las posiciones (llenas y vacías)
     * @return estructura completa para mostrar en frontend
     */
    public TournamentStructureDTO buildTournamentStructure(Tournament tournament, 
                                                         List<TeamPositionDTO> positions) {
        if (tournament == null) {
            return null;
        }
        
        Map<String, List<TeamPositionDTO>> groups = new HashMap<>();
        List<TeamPositionDTO> bracket = new ArrayList<>();
        
        // Organizar posiciones según el formato del torneo
        for (TeamPositionDTO position : positions) {
            if (position.getGroupName() != null) {
                // Es fase de grupos
                groups.computeIfAbsent(position.getGroupName(), k -> new ArrayList<>()).add(position);
            } else {
                // Es eliminación directa
                bracket.add(position);
            }
        }
        
        // Ordenar dentro de cada grupo/bracket
        groups.values().forEach(groupPositions -> 
            groupPositions.sort((a, b) -> Integer.compare(
                a.getPosition() != null ? a.getPosition() : 0,
                b.getPosition() != null ? b.getPosition() : 0
            ))
        );
        
        bracket.sort((a, b) -> Integer.compare(
            a.getPosition() != null ? a.getPosition() : 0,
            b.getPosition() != null ? b.getPosition() : 0
        ));
        
        long filledPositions = positions.stream().mapToLong(p -> p.getIsEmpty() ? 0 : 1).sum();
        
        return TournamentStructureDTO.builder()
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .format(tournament.getTournamentFormat().name())
                .groups(groups.isEmpty() ? null : groups)
                .bracket(bracket.isEmpty() ? null : bracket)
                .totalPositions(tournament.getTotalTeams())
                .filledPositions((int) filledPositions)
                .isComplete(filledPositions == tournament.getTotalTeams())
                .build();
    }
    
    // =====================================
    // MÉTODOS AUXILIARES PARA CÁLCULOS
    // =====================================
    
    /**
     * Calcula si el torneo puede recibir más jugadores
     * @param tournament entidad del torneo
     * @return true si puede unirse más gente
     */
    private Boolean calculateCanJoin(Tournament tournament) {
        return tournament.getStatus() == TournamentStatus.CREATED || 
               tournament.getStatus() == TournamentStatus.WAITING_PLAYERS &&
               tournament.getCurrentPlayers() < tournament.getMaxPlayers();
    }
    
    /**
     * Calcula si el torneo ya comenzó
     * @param tournament entidad del torneo
     * @return true si ya está en progreso o terminado
     */
    private Boolean calculateIsStarted(Tournament tournament) {
        return tournament.getStatus() == TournamentStatus.IN_PROGRESS ||
               tournament.getStatus() == TournamentStatus.FINISHED;
    }
    
    /**
     * Genera posiciones vacías para completar la estructura del torneo
     * @param tournament datos del torneo
     * @param existingPositions posiciones ya ocupadas
     * @return lista completa incluyendo posiciones vacías
     */
    public List<TeamPositionDTO> generateCompleteStructure(Tournament tournament, 
                                                         List<TeamPositionDTO> existingPositions) {
        List<TeamPositionDTO> completeStructure = new ArrayList<>(existingPositions);
        
        // Determinar posiciones faltantes según formato
        switch (tournament.getTournamentFormat()) {
            case GROUPS_THEN_KNOCKOUT:
                completeStructure.addAll(generateMissingGroupPositions(tournament, existingPositions));
                break;
            case DIRECT_KNOCKOUT:
                completeStructure.addAll(generateMissingBracketPositions(tournament, existingPositions));
                break;
        }
        
        return completeStructure;
    }
    
    /**
     * Genera posiciones faltantes para fase de grupos
     */
    private List<TeamPositionDTO> generateMissingGroupPositions(Tournament tournament, 
                                                              List<TeamPositionDTO> existing) {
        List<TeamPositionDTO> missing = new ArrayList<>();
        
        // Lógica para calcular grupos faltantes
        // Por ejemplo: 4 equipos por grupo, grupos A, B, C, D
        int teamsPerGroup = 4;
        int totalGroups = tournament.getTotalTeams() / teamsPerGroup;
        
        for (int group = 0; group < totalGroups; group++) {
            String groupName = String.valueOf((char) ('A' + group));
            
            for (int position = 1; position <= teamsPerGroup; position++) {
                final String finalGroupName = groupName;
                final int finalPosition = position;
                
                boolean exists = existing.stream().anyMatch(p -> 
                    finalGroupName.equals(p.getGroupName()) && 
                    finalPosition == (p.getPosition() != null ? p.getPosition() : 0)
                );
                
                if (!exists) {
                    missing.add(createEmptyPosition(position, groupName));
                }
            }
        }
        
        return missing;
    }
    
    /**
     * Genera posiciones faltantes para eliminación directa
     */
    private List<TeamPositionDTO> generateMissingBracketPositions(Tournament tournament, 
                                                                List<TeamPositionDTO> existing) {
        List<TeamPositionDTO> missing = new ArrayList<>();
        
        for (int position = 1; position <= tournament.getTotalTeams(); position++) {
            final int finalPosition = position;
            boolean exists = existing.stream().anyMatch(p -> 
                finalPosition == (p.getPosition() != null ? p.getPosition() : 0)
            );
            
            if (!exists) {
                missing.add(createEmptyPosition(position, null));
            }
        }
        
        return missing;
    }
}