package com.pes.copa.matches.service;

import com.pes.copa.teams.entity.Teams;
import com.pes.copa.teams.repository.TeamsRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Servicio para gestionar las operaciones relacionadas con equipos de clubes
 * @author sangr
 */
@Service
public class TeamsService {

    private final TeamsRepository teamsRepository;

    @Autowired
    public TeamsService(TeamsRepository teamsRepository) {
        this.teamsRepository = teamsRepository;
    }

    /**
     * Busca todos los equipos de un continente específico.
     * @param continent nombre del continente
     * @return lista de equipos del continente
     * @throws IllegalArgumentException si el continente es nulo o vacío
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public List<Teams> getTeamsByContinent(String continent) {
        if (continent == null || continent.trim().isEmpty()) {
            throw new IllegalArgumentException("El continente no puede ser nulo o vacío");
        }
        
        try {
            return teamsRepository.findByContinent(continent.trim());
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Busca todos los equipos de un país específico.
     * @param country nombre del país
     * @return lista de equipos del país
     * @throws IllegalArgumentException si el país es nulo o vacío
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public List<Teams> getTeamsByCountry(String country) {
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("El país no puede ser nulo o vacío");
        }
        
        try {
            return teamsRepository.findByCountry(country.trim());
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Busca un equipo por su ID.
     * @param id identificador del equipo
     * @return el equipo encontrado
     * @throws IllegalArgumentException si el ID es nulo
     * @throws RuntimeException si no se encuentra el equipo o hay problemas de BD
     */
    public Teams getTeamById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        
        try {
            Optional<Teams> team = teamsRepository.findById(id);
            if (team.isEmpty()) {
                throw new RuntimeException("Equipo no encontrado con ID: " + id);
            }
            return team.get();
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    public List<Teams> getTeamsByIsChampions() {
        try {
            return teamsRepository.findByIsChampionsTrue();
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Retorna todos los equipos registrados.
     * @return lista de todos los equipos
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public List<Teams> getAllTeams() {
        try {
            return teamsRepository.findAll();
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Guarda o actualiza un equipo.
     * @param team equipo a guardar
     * @return equipo guardado
     * @throws IllegalArgumentException si el equipo es nulo
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public Teams saveTeam(Teams team) {
        if (team == null) {
            throw new IllegalArgumentException("El equipo no puede ser nulo");
        }
        
        try {
            return teamsRepository.save(team);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al guardar el equipo en la base de datos", e);
        }
    }

    /**
     * Elimina un equipo por ID.
     * @param id identificador del equipo a eliminar
     * @throws IllegalArgumentException si el ID es nulo
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public void deleteTeam(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        
        try {
            if (!teamsRepository.existsById(id)) {
                throw new RuntimeException("Equipo no encontrado con ID: " + id);
            }
            teamsRepository.deleteById(id);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
}