package com.pes.copa.tournaments.controller;

import com.pes.copa.tournaments.dto.request.AssignTeamDTO;
import com.pes.copa.tournaments.dto.request.FillRandomTeamsDTO;
import com.pes.copa.tournaments.dto.response.AvailableTeamDTO;
import com.pes.copa.tournaments.dto.response.TournamentStructureDTO;
import com.pes.copa.tournaments.service.TournamentSetUpService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para configuración y setup de torneos
 * Maneja la asignación de equipos, estructura y configuración inicial
 * @author sangr
 */
@RestController
@RequestMapping("/api/v1/tournaments")
public class TournamentSetUpController {
    
    private final TournamentSetUpService tournamentSetupService;
    
    @Autowired
    public TournamentSetUpController(TournamentSetUpService tournamentSetupService) {
        this.tournamentSetupService = tournamentSetupService;
    }
    
    /**
     * Obtiene equipos disponibles para un torneo específico
     * @param tournamentId ID del torneo
     * @return lista de equipos que pueden participar
     */
    @GetMapping("/{tournamentId}/available-teams")
    public ResponseEntity<List<AvailableTeamDTO>> getAvailableTeams(@PathVariable Long tournamentId) {
        try {
            List<AvailableTeamDTO> teams = tournamentSetupService.getAvailableTeams(tournamentId);
            return ResponseEntity.ok(teams);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene la estructura actual del torneo (grupos/bracket con posiciones)
     * @param tournamentId ID del torneo
     * @return estructura completa del torneo
     */
    @GetMapping("/{tournamentId}/structure")
    public ResponseEntity<TournamentStructureDTO> getTournamentStructure(@PathVariable Long tournamentId) {
        try {
            TournamentStructureDTO structure = tournamentSetupService.getTournamentStructure(tournamentId);
            return ResponseEntity.ok(structure);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Asigna un equipo a una posición específica en el torneo
     * @param tournamentId ID del torneo
     * @param dto datos de la asignación (equipo, jugador, posición)
     * @return estructura actualizada del torneo
     */
    @PostMapping("/{tournamentId}/assign-team")
    public ResponseEntity<TournamentStructureDTO> assignTeamToPosition(@PathVariable Long tournamentId,
                                                                     @Valid @RequestBody AssignTeamDTO dto) {
        try {
            TournamentStructureDTO structure = tournamentSetupService.assignTeamToPosition(tournamentId, dto);
            return ResponseEntity.ok(structure);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Conflicto si posición ocupada
        }
    }
    
    /**
     * Llena automáticamente las posiciones vacantes con equipos aleatorios
     * @param tournamentId ID del torneo
     * @param dto configuración para el llenado automático
     * @return estructura actualizada del torneo
     */
    @PostMapping("/{tournamentId}/fill-random")
    public ResponseEntity<TournamentStructureDTO> fillRandomTeams(@PathVariable Long tournamentId,
                                                                @RequestBody FillRandomTeamsDTO dto) {
        try {
            TournamentStructureDTO structure = tournamentSetupService.fillRandomTeams(tournamentId, dto);
            return ResponseEntity.ok(structure);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    
    
    /**
     * Reinicia completamente el setup del torneo (remueve todos los equipos)
     * @param tournamentId ID del torneo
     * @return estructura vacía del torneo
     */
    @PostMapping("/{tournamentId}/reset")
    public ResponseEntity<TournamentStructureDTO> resetTournamentSetup(@PathVariable Long tournamentId) {
        try {
            TournamentStructureDTO structure = tournamentSetupService.resetTournamentSetup(tournamentId);
            return ResponseEntity.ok(structure);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Finaliza el setup y prepara el torneo para comenzar
     * @param tournamentId ID del torneo
     * @return estructura finalizada del torneo
     */
    @PostMapping("/{tournamentId}/finalize")
    public ResponseEntity<TournamentStructureDTO> finalizeTournamentSetup(@PathVariable Long tournamentId) {
        try {
            TournamentStructureDTO structure = tournamentSetupService.finalizeTournamentSetup(tournamentId);
            return ResponseEntity.ok(structure);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Si no está completo el setup
        }
    }
}