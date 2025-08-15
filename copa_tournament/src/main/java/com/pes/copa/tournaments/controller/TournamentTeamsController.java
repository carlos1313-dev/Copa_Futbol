package com.pes.copa.tournaments.controller;

import com.pes.copa.tournaments.dto.request.AssignTeamDTO;
import com.pes.copa.tournaments.dto.request.FillRandomTeamsDTO;
import com.pes.copa.tournaments.dto.response.AvailableTeamDTO;
import com.pes.copa.tournaments.dto.response.TeamPositionDTO;
import com.pes.copa.tournaments.dto.response.TournamentStructureDTO;
import com.pes.copa.tournaments.service.TournamentTeamsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para consultas específicas de equipos en torneos
 * @author sangr
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tournaments/{tournamentId}/teams")
public class TournamentTeamsController {
    
    private final TournamentTeamsService tournamentTeamService;
    
    @Autowired
    public TournamentTeamsController(TournamentTeamsService tournamentTeamService) {
        this.tournamentTeamService = tournamentTeamService;
    }
    
    /**
     * Obtiene la estructura completa del torneo con equipos asignados
     * @param tournamentId ID del torneo
     * @return estructura del torneo
     */
    @GetMapping("/structure")
    public ResponseEntity<TournamentStructureDTO> getTournamentStructure(@PathVariable Long tournamentId) {
        try {
            TournamentStructureDTO structure = tournamentTeamService.getTournamentStructure(tournamentId);
            return ResponseEntity.ok(structure);
        } catch (IllegalArgumentException e) {
            log.warn("Parámetros inválidos para obtener estructura del torneo {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error al obtener estructura del torneo {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene todos los equipos disponibles para seleccionar en el torneo
     * @param tournamentId ID del torneo
     * @return lista de equipos disponibles con estado
     */
    @GetMapping("/available")
    public ResponseEntity<List<AvailableTeamDTO>> getAvailableTeams(@PathVariable Long tournamentId) {
        try {
            List<AvailableTeamDTO> availableTeams = tournamentTeamService.getAvailableTeams(tournamentId);
            return ResponseEntity.ok(availableTeams);
        } catch (IllegalArgumentException e) {
            log.warn("Parámetros inválidos para obtener equipos disponibles del torneo {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error al obtener equipos disponibles del torneo {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Asigna un equipo a una posición específica en el torneo
     * @param tournamentId ID del torneo
     * @param assignTeamDTO datos de asignación
     * @return equipo asignado con información completa
     */
    @PostMapping("/assign")
    public ResponseEntity<TeamPositionDTO> assignTeamToPosition(@PathVariable Long tournamentId,
                                                              @Valid @RequestBody AssignTeamDTO assignTeamDTO) {
        try {
            TeamPositionDTO assignedTeam = tournamentTeamService.assignTeamToPosition(tournamentId, assignTeamDTO);
            log.info("Equipo {} asignado al torneo {} en posición {}", 
                    assignTeamDTO.getTeamId(), tournamentId, assignTeamDTO.getPosition());
            return ResponseEntity.status(HttpStatus.CREATED).body(assignedTeam);
        } catch (IllegalArgumentException e) {
            log.warn("Parámetros inválidos para asignar equipo al torneo {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error al asignar equipo al torneo {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Llena automáticamente las posiciones vacantes con equipos aleatorios
     * @param tournamentId ID del torneo
     * @param fillRandomDTO configuración para llenado automático
     * @return estructura actualizada del torneo
     */
    @PostMapping("/fill-random")
    public ResponseEntity<TournamentStructureDTO> fillRandomTeams(@PathVariable Long tournamentId,
                                                                @RequestBody FillRandomTeamsDTO fillRandomDTO) {
        try {
            TournamentStructureDTO structure = tournamentTeamService.fillRandomTeams(tournamentId, fillRandomDTO);
            log.info("Llenado automático completado para torneo {}", tournamentId);
            return ResponseEntity.ok(structure);
        } catch (IllegalArgumentException e) {
            log.warn("Parámetros inválidos para llenado automático del torneo {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error al llenar automáticamente el torneo {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Elimina un equipo del torneo
     * @param tournamentId ID del torneo
     * @param teamId ID del equipo a eliminar
     * @return respuesta vacía
     */
    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> removeTeamFromTournament(@PathVariable Long tournamentId,
                                                       @PathVariable Long teamId) {
        try {
            tournamentTeamService.removeTeamFromTournament(tournamentId, teamId);
            log.info("Equipo {} removido del torneo {}", teamId, tournamentId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Parámetros inválidos para remover equipo {} del torneo {}: {}", teamId, tournamentId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error al remover equipo {} del torneo {}: {}", teamId, tournamentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene todos los equipos de un torneo con información detallada
     * @param tournamentId ID del torneo
     * @return lista de equipos en el torneo
     */
    @GetMapping("/{id}")
    public ResponseEntity<List<TeamPositionDTO>> getTeamsByTournament(@PathVariable Long tournamentId) {
        try {
            List<TeamPositionDTO> teams = tournamentTeamService.getTeamsByTournament(tournamentId);
            return ResponseEntity.ok(teams);
        } catch (IllegalArgumentException e) {
            log.warn("Parámetros inválidos para obtener equipos del torneo {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error al obtener equipos del torneo {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene equipos de un grupo específico
     * @param tournamentId ID del torneo
     * @param groupName nombre del grupo (A, B, C, etc.)
     * @return lista de equipos del grupo
     */
    @GetMapping("/group/{groupName}")
    public ResponseEntity<List<TeamPositionDTO>> getTeamsByGroup(@PathVariable Long tournamentId,
                                                              @PathVariable String groupName) {
        try {
            List<TeamPositionDTO> teams = tournamentTeamService.getTeamsByGroup(tournamentId, groupName);
            return ResponseEntity.ok(teams);
        } catch (IllegalArgumentException e) {
            log.warn("Parámetros inválidos para obtener equipos del grupo {} en torneo {}: {}", 
                    groupName, tournamentId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error al obtener equipos del grupo {} en torneo {}: {}", 
                    groupName, tournamentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene equipos controlados por un jugador específico
     * @param tournamentId ID del torneo
     * @param playerId ID del jugador
     * @return lista de equipos del jugador
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<TeamPositionDTO>> getTeamsByPlayer(@PathVariable Long tournamentId,
                                                               @PathVariable Long playerId) {
        try {
            List<TeamPositionDTO> teams = tournamentTeamService.getTeamsByPlayer(tournamentId, playerId);
            return ResponseEntity.ok(teams);
        } catch (IllegalArgumentException e) {
            log.warn("Parámetros inválidos para obtener equipos del jugador {} en torneo {}: {}", 
                    playerId, tournamentId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error al obtener equipos del jugador {} en torneo {}: {}", 
                    playerId, tournamentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene equipos que aún están activos (no eliminados)
     * @param tournamentId ID del torneo
     * @return lista de equipos activos
     */
    @GetMapping("/active")
    public ResponseEntity<List<TeamPositionDTO>> getActiveTeams(@PathVariable Long tournamentId) {
        try {
            List<TeamPositionDTO> teams = tournamentTeamService.getActiveTeams(tournamentId);
            return ResponseEntity.ok(teams);
        } catch (IllegalArgumentException e) {
            log.warn("Parámetros inválidos para obtener equipos activos del torneo {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error al obtener equipos activos del torneo {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}