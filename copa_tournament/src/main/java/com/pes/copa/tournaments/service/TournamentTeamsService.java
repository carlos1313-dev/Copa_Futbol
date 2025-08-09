package com.pes.copa.tournaments.service;

import com.pes.copa.tournaments.dto.request.AssignTeamDTO;
import com.pes.copa.tournaments.entity.Tournament;
import com.pes.copa.tournaments.entity.TournamentTeam;
import com.pes.copa.tournaments.enums.TeamType;
import com.pes.copa.tournaments.repository.TournamentRepository;
import com.pes.copa.tournaments.repository.TournamentTeamRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar las operaciones relacionadas con equipos en torneos
 * @author sangr
 */
@Service
public class TournamentTeamsService {
    
    private final TournamentTeamRepository tournamentTeamRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentService tournamentService;
    
    @Autowired
    public TournamentTeamsService(TournamentTeamRepository tournamentTeamRepository,
                               TournamentRepository tournamentRepository,
                               TournamentService tournamentService) {
        this.tournamentTeamRepository = tournamentTeamRepository;
        this.tournamentRepository = tournamentRepository;
        this.tournamentService = tournamentService;
    }
    
    /**
     * Asigna un equipo a una posición específica en el torneo
     * @param tournamentId ID del torneo
     * @param dto datos de asignación del equipo
     * @return equipo asignado
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws RuntimeException si hay problemas de acceso a datos o validación
     */
    @Transactional
    public TournamentTeam assignTeamToPosition(Long tournamentId, AssignTeamDTO dto) {
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
            
            // Crear nueva asignación
            TournamentTeam tournamentTeam = new TournamentTeam();
            tournamentTeam.setTournamentId(tournamentId);
            tournamentTeam.setTeamId(dto.getTeamId());
            tournamentTeam.setPlayerId(dto.getPlayerId());
            tournamentTeam.setGroupName(dto.getGroupName());
            tournamentTeam.setPosition(dto.getPosition());
            tournamentTeam.setIsEliminated(false);
            
            // Determinar tipo de equipo basado en el tipo de torneo
            tournamentTeam.setTeamType(determineTeamType(tournament));
            
            TournamentTeam saved = tournamentTeamRepository.save(tournamentTeam);
            
            // Actualizar contador de jugadores en el torneo
            tournamentService.updateCurrentPlayers(tournamentId);
            
            return saved;
            
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al asignar equipo al torneo en la base de datos", e);
        }
    }
    
    /**
     * Obtiene todos los equipos de un torneo
     * @param tournamentId ID del torneo
     * @return lista de equipos del torneo
     * @throws IllegalArgumentException si el tournamentId es nulo
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public List<TournamentTeam> getTeamsByTournament(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El ID del torneo no puede ser nulo");
        }
        
        try {
            return tournamentTeamRepository.findByTournamentIdOrderByGroupNameAscPositionAsc(tournamentId);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    /**
     * Obtiene equipos de un grupo específico
     * @param tournamentId ID del torneo
     * @param groupName nombre del grupo
     * @return lista de equipos del grupo
     * @throws IllegalArgumentException si los parámetros son nulos
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public List<TournamentTeam> getTeamsByGroup(Long tournamentId, String groupName) {
        if (tournamentId == null || groupName == null) {
            throw new IllegalArgumentException("El ID del torneo y el nombre del grupo no pueden ser nulos");
        }
        
        try {
            return tournamentTeamRepository.findByTournamentIdAndGroupName(tournamentId, groupName);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    /**
     * Elimina un equipo del torneo
     * @param tournamentId ID del torneo
     * @param teamId ID del equipo
     * @throws IllegalArgumentException si los parámetros son nulos
     * @throws RuntimeException si hay problemas de acceso a datos
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
            
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al eliminar equipo del torneo en la base de datos", e);
        }
    }
    
    /**
     * Obtiene equipos controlados por un jugador específico en un torneo
     * @param tournamentId ID del torneo
     * @param playerId ID del jugador
     * @return lista de equipos del jugador
     * @throws IllegalArgumentException si los parámetros son nulos
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public List<TournamentTeam> getTeamsByPlayer(Long tournamentId, Long playerId) {
        if (tournamentId == null || playerId == null) {
            throw new IllegalArgumentException("El ID del torneo y del jugador no pueden ser nulos");
        }
        
        try {
            return tournamentTeamRepository.findByTournamentIdAndPlayerId(tournamentId, playerId);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    public List<TournamentTeam> getTeamsByIsEliminatedFalse(Long tournamentId) {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El ID del torneo no puede ser nulo");
        } 
        try {
            return tournamentTeamRepository.findByTournamentIdAndIsEliminatedFalse(tournamentId);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    /**
     * Determina el tipo de equipo basado en el tipo de torneo
     * @param tournament el torneo
     * @return tipo de equipo
     */
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
                return TeamType.COUNTRY; // Por defecto
        }
    }
}