package com.pes.copa.teams.controller;

import com.pes.copa.teams.entity.Teams;
import com.pes.copa.teams.service.TeamsService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestionar operaciones con equipos de clubes
 * @author sangr
 */
@RestController
@RequestMapping("/api/v1/teams")
public class TeamsController {
    
    private final TeamsService teamsService;
    
    @Autowired
    public TeamsController(TeamsService teamsService) {
        this.teamsService = teamsService;
    }
    
    /**
     * Obtiene todos los equipos de un país específico
     * @param country nombre del país
     * @return lista de equipos del país
     */
    @GetMapping("/country/{country}")
    public ResponseEntity<List<Teams>> getTeamsByCountry(@PathVariable String country) {
        try {
            List<Teams> teams = teamsService.getTeamsByCountry(country);
            return ResponseEntity.ok(teams);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene todos los equipos de un continente específico
     * @param continent nombre del continente
     * @return lista de equipos del continente
     */
    @GetMapping("/continent/{continent}")
    public ResponseEntity<List<Teams>> getTeamsByContinent(@PathVariable String continent) {
        try {
            List<Teams> teams = teamsService.getTeamsByContinent(continent);
            return ResponseEntity.ok(teams);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene todas las selecciones mundialistas
     * @return lista de países mundialistas
     */
    @GetMapping("/champions")
    public ResponseEntity<List<Teams>> getTeamsByIsChampions() {
        try {
            List<Teams> teams = teamsService.getTeamsByIsChampions();
            return ResponseEntity.ok(teams);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene un equipo por su ID
     * @param id identificador del equipo
     * @return el equipo encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<Teams> getTeamById(@PathVariable Long id) {
        try {
            Teams team = teamsService.getTeamById(id);
            return ResponseEntity.ok(team);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            // Puede ser que no se encontró o problema de BD
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Obtiene todos los equipos
     * @return lista de todos los equipos
     */
    @GetMapping
    public ResponseEntity<List<Teams>> getAllTeams() {
        try {
            List<Teams> teams = teamsService.getAllTeams();
            return ResponseEntity.ok(teams);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}