package com.pes.copa.teams.controller;

import com.pes.copa.teams.entity.Country;
import com.pes.copa.teams.service.CountryService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestionar operaciones con países/selecciones
 * @author sangr
 */
@RestController
@RequestMapping("/api/v1/countries")
public class CountryController {
    
    private final CountryService countryService;
    
    @Autowired
    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }
    
    /**
     * Obtiene todas las selecciones de un continente específico
     * @param continent nombre del continente
     * @return lista de países del continente
     */
    @GetMapping("/continent/{continent}")
    public ResponseEntity<List<Country>> getCountriesByContinent(@PathVariable String continent) {
        try {
            List<Country> countries = countryService.getCountriesByContinent(continent);
            return ResponseEntity.ok(countries);
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
    @GetMapping("/mundialistas")
    public ResponseEntity<List<Country>> getCountriesByIsMundialist() {
        try {
            List<Country> countries = countryService.getCountriesByIsMundialist();
            return ResponseEntity.ok(countries);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
    
    /**
     * Obtiene un país por su ID
     * @param id identificador del país
     * @return el país encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<Country> getCountryById(@PathVariable Long id) {
        try {
            Country country = countryService.getCountryById(id);
            return ResponseEntity.ok(country);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            // Puede ser que no se encontró o problema de BD
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Obtiene todos los países
     * @return lista de todos los países
     */
    @GetMapping
    public ResponseEntity<List<Country>> getAllCountries() {
        try {
            List<Country> countries = countryService.getAllCountries();
            return ResponseEntity.ok(countries);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}