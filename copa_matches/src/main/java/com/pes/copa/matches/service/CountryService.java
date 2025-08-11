package com.pes.copa.matches.service;

import com.pes.copa.teams.entity.Country;
import com.pes.copa.teams.repository.CountryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Servicio para gestionar las operaciones relacionadas con países/selecciones
 * @author sangr
 */
@Service
public class CountryService {
    
    private final CountryRepository countryRepository;
    
    @Autowired
    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }
    
    /**
     * Busca todas las selecciones de un continente.
     * @param continent nombre del continente
     * @return lista de países del continente
     * @throws IllegalArgumentException si el continente es nulo o vacío
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public List<Country> getCountriesByContinent(String continent) {
        if (continent == null || continent.trim().isEmpty()) {
            throw new IllegalArgumentException("El continente no puede ser nulo o vacío");
        }
        
        try {
            return countryRepository.findByContinent(continent.trim());
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    /**
     * Busca todas las selecciones mundialistas.
     * @return lista de países mundialistas
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public List<Country> getCountriesByIsMundialist() {
        try {
            return countryRepository.findByIsMundialistTrue();
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    /**
     * Busca un país por su ID.
     * @param id identificador del país
     * @return el país encontrado
     * @throws IllegalArgumentException si el ID es nulo
     * @throws RuntimeException si no se encuentra el país o hay problemas de BD
     */
    public Country getCountryById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        
        try {
            Optional<Country> country = countryRepository.findById(id);
            if (country.isEmpty()) {
                throw new RuntimeException("País no encontrado con ID: " + id);
            }
            return country.get();
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
    
    /**
     * Retorna todos los países registrados.
     * @return lista de todos los países
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public List<Country> getAllCountries() {
        try {
            return countryRepository.findAll();
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * Elimina un país por ID.
     * @param id identificador del país a eliminar
     * @throws IllegalArgumentException si el ID es nulo
     * @throws RuntimeException si hay problemas de acceso a datos
     */
    public void deleteCountry(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        
        try {
            if (!countryRepository.existsById(id)) {
                throw new RuntimeException("País no encontrado con ID: " + id);
            }
            countryRepository.deleteById(id);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
    }
}