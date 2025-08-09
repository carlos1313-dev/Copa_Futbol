package com.pes.copa.tournaments.controller;

import com.pes.copa.tournaments.dto.response.TeamPositionDTO;
import com.pes.copa.tournaments.entity.TournamentTeam;
import com.pes.copa.tournaments.service.TournamentTeamsService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para consultas específicas de equipos en torneos
 * @author sangr
 */
@RestController
@RequestMapping("/api/v1/tournaments/{tournamentId}/teams")
public class TournamentTeamsController {
    
    private final TournamentTeamsService tournamentTeamService;
    
    @Autowired
    public TournamentTeamsController(TournamentTeamsService tournamentTeamService) {
        this.tournamentTeamService = tournamentTeamService;
    }
    
    /**
     * Obtiene todos los equipos de un torneo
     * @param tournamentId ID del torneo
     * @return lista de equipos en el torneo
     */
    @GetMapping
    public ResponseEntity<List<TournamentTeam>> getTeamsByTournament(@PathVariable Long tournamentId) {
        try {
            List<TournamentTeam> teams = tournamentTeamService.getTeamsByTournament(tournamentId);
            return ResponseEntity.ok(teams);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
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
    public ResponseEntity<List<TournamentTeam>> getTeamsByGroup(@PathVariable Long tournamentId,
                                                              @PathVariable String groupName) {
        try {
            List<TournamentTeam> teams = tournamentTeamService.getTeamsByGroup(tournamentId, groupName);
            return ResponseEntity.ok(teams);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
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
    public ResponseEntity<List<TournamentTeam>> getTeamsByPlayer(@PathVariable Long tournamentId,
                                                               @PathVariable Long playerId) {
        try {
            List<TournamentTeam> teams = tournamentTeamService.getTeamsByPlayer(tournamentId, playerId);
            return ResponseEntity.ok(teams);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene equipos controlados por IA (sin jugador asignado)
     * @param tournamentId ID del torneo
     * @return lista de equipos de IA
     */
//    @GetMapping("/ai")
//    public ResponseEntity<List<TournamentTeam>> getAITeams(@PathVariable Long tournamentId) {
//        try {
//            List<TournamentTeam> teams = tournamentTeamService.getAITeams(tournamentId);
//            return ResponseEntity.ok(teams);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().build();
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
//        }
//    }
    
    /**
     * Obtiene equipos que aún están activos (no eliminados)
     * @param tournamentId ID del torneo
     * @return lista de equipos activos
     */
    @GetMapping("/active")
    public ResponseEntity<List<TournamentTeam>> getActiveTeams(@PathVariable Long tournamentId) {
        try {
            List<TournamentTeam> teams = tournamentTeamService.getTeamsByIsEliminatedFalse(tournamentId);
            return ResponseEntity.ok(teams);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}