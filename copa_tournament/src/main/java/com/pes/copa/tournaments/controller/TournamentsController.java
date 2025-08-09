package com.pes.copa.tournaments.controller;

import com.pes.copa.tournaments.dto.request.CreateTournamentDTO;
import com.pes.copa.tournaments.dto.request.JoinTournamentDTO;
import com.pes.copa.tournaments.dto.response.TournamentDTO;
import com.pes.copa.tournaments.enums.TournamentStatus;
import com.pes.copa.tournaments.enums.TournamentType;
import com.pes.copa.tournaments.service.TournamentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestionar operaciones con torneos
 * @author sangr
 */
@RestController
@RequestMapping("/api/v1/tournaments")
public class TournamentController {
    
    private final TournamentService tournamentService;
    
    @Autowired
    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }
    
    /**
     * Crea un nuevo torneo
     * @param dto datos del torneo a crear
     * @return torneo creado
     */
    @PostMapping
    public ResponseEntity<TournamentDTO> createTournament(@Valid @RequestBody CreateTournamentDTO dto) {
        try {
            TournamentDTO tournament = tournamentService.createTournament(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(tournament);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene un torneo por su ID
     * @param id identificador del torneo
     * @return el torneo encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<TournamentDTO> getTournamentById(@PathVariable Long id) {
        try {
            TournamentDTO tournament = tournamentService.getTournamentById(id);
            return ResponseEntity.ok(tournament);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Obtiene todos los torneos por estado
     * @param status estado del torneo (CREATED, WAITING_PLAYERS, IN_PROGRESS, FINISHED)
     * @return lista de torneos
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TournamentDTO>> getTournamentsByStatus(@PathVariable String status) {
        try {
            TournamentStatus tournamentStatus = TournamentStatus.valueOf(status.toUpperCase());
            List<TournamentDTO> tournaments = tournamentService.getTournamentsByStatus(tournamentStatus);
            return ResponseEntity.ok(tournaments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene torneos creados por un usuario específico
     * @param userId ID del usuario
     * @return lista de torneos del usuario
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TournamentDTO>> getTournamentsByUser(@PathVariable Long userId) {
        try {
            List<TournamentDTO> tournaments = tournamentService.getTournamentsByUser(userId);
            return ResponseEntity.ok(tournaments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene torneos disponibles para unirse
     * @return lista de torneos que aceptan nuevos jugadores
     */
    @GetMapping("/available")
    public ResponseEntity<List<TournamentDTO>> getAvailableTournaments() {
        try {
            List<TournamentDTO> tournaments = tournamentService.getAvailableTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene torneos por tipo
     * @param type tipo de torneo (WORLD_CUP, COPA_AMERICA, etc.)
     * @return lista de torneos del tipo especificado
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<TournamentDTO>> getTournamentsByType(@PathVariable String type) {
        try {
            TournamentType tournamentType = TournamentType.valueOf(type.toUpperCase());
            List<TournamentDTO> tournaments = tournamentService.getTournamentsByType(tournamentType);
            return ResponseEntity.ok(tournaments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Actualiza el estado de un torneo
     * @param id ID del torneo
     * @param status nuevo estado
     * @return torneo actualizado
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<TournamentDTO> updateTournamentStatus(@PathVariable Long id, 
                                                               @RequestParam String status) {
        try {
            TournamentStatus newStatus = TournamentStatus.valueOf(status.toUpperCase());
            TournamentDTO tournament = tournamentService.updateTournamentStatus(id, newStatus);
            return ResponseEntity.ok(tournament);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Elimina un torneo
     * @param id ID del torneo a eliminar
     * @return respuesta vacía
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTournament(@PathVariable Long id) {
        try {
            tournamentService.deleteTournament(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}