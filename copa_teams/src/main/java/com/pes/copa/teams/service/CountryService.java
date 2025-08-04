/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.teams.service;

import com.pes.copa.teams.entity.Country;
import com.pes.copa.teams.entity.Teams;
import com.pes.copa.teams.repository.CountryRepository;
import com.pes.copa.teams.repository.TeamsRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author sangr
 */
public class CountryService {
    private final CountryRepository countryRepository;
    
    @Autowired
    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }
    
    /**
     * Busca todas las selecciones de un continente.
     */
    public List<Country> getCountriesByContinent(String continent) {
        return countryRepository.findCountryByContinent(continent);
    }
    
    
    
    /**
     * Retorna todos los equipos registrados.
     */
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

//    /**
//     * Guarda o actualiza un equipo.
//     */
//    public Country saveCountry(Country country) {
//        return countryRepository.save(country);
//    }

    /**
     * Elimina un equipo por ID.
     */
    public void deleteCountry(Long id) {
        countryRepository.deleteById(id);
    }
}
