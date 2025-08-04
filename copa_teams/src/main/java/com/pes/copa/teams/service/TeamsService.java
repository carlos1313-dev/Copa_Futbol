/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.teams.service;

import com.pes.copa.teams.entity.Teams;
import com.pes.copa.teams.repository.TeamsRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author sangr
 */
public class TeamsService {

    private final TeamsRepository teamsRepository;

    @Autowired
    public TeamsService(TeamsRepository teamsRepository) {
        this.teamsRepository = teamsRepository;
    }

    /**
     * Busca todas las selecciones de un continente.
     */
    public List<Teams> getCountriesByContinent(String continent) {
        return teamsRepository.findCountryByContinent(continent);
    }

    /**
     * Busca todos los equipos de un continente específico.
     */
    public List<Teams> getTeamsByContinent(String continent) {
        return teamsRepository.findTeamByContinent(continent);
    }

    /**
     * Busca todas las selecciones de un país.
     */
    public List<Teams> getTeamsByCountry(String country) {
        return teamsRepository.findTeamByCountry(country);
    }

    /**
     * Retorna todos los equipos registrados.
     */
    public List<Teams> getAllTeams() {
        return teamsRepository.findAll();
    }

    /**
     * Guarda o actualiza un equipo.
     */
    public Teams saveTeam(Teams team) {
        return teamsRepository.save(team);
    }

    /**
     * Elimina un equipo por ID.
     */
    public void deleteTeam(Long id) {
        teamsRepository.deleteById(id);
    }
}
