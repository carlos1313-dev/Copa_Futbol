package com.pes.copa.tournaments.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pes.copa.tournaments.dto.external.CountryDTO;
import com.pes.copa.tournaments.dto.external.TeamDTO;
import com.pes.copa.tournaments.enums.TournamentType;
import java.time.Duration;
import java.util.List;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Cliente para comunicarse con el Teams Service
 * Maneja la obtención de equipos y países según el tipo de torneo
 * @author sangr
 */
@Slf4j
@Service
public class TeamsServiceClient {
    
    private final WebClient teamsWebClient;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    
    @Autowired
    public TeamsServiceClient(WebClient teamsWebClient) {
        this.teamsWebClient = teamsWebClient;
    }
    
    /**
     * Obtiene países/selecciones disponibles según el tipo de torneo
     * @param tournamentType tipo de torneo
     * @return lista de países disponibles
     * @throws RuntimeException si hay error en la comunicación
     */
    public List<CountryDTO> getCountriesByTournamentType(TournamentType tournamentType) {
        try {
            log.info("Obteniendo países para tipo de torneo: {}", tournamentType);
            
            List<CountryDTO> countries = teamsWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/countries/tournament-type/{type}")
                            .build(tournamentType.name()))
                    .retrieve()
                    .bodyToFlux(CountryDTO.class)
                    .timeout(TIMEOUT)
                    .collectList()
                    .block();
            
            log.info("Obtenidos {} países para torneo {}", 
                    countries != null ? countries.size() : 0, tournamentType);
            
            return countries != null ? countries : Collections.emptyList();
            
        } catch (WebClientResponseException e) {
            log.error("Error HTTP al obtener países para {}: {} - {}", 
                    tournamentType, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al obtener países del Teams Service: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("Error inesperado al obtener países para {}: {}", tournamentType, e.getMessage(), e);
            throw new RuntimeException("Error de comunicación con Teams Service", e);
        }
    }
    
    /**
     * Obtiene todos los países disponibles
     * @return lista de todos los países
     * @throws RuntimeException si hay error en la comunicación
     */
    public List<CountryDTO> getAllCountries() {
        try {
            log.info("Obteniendo todos los países");
            
            List<CountryDTO> countries = teamsWebClient.get()
                    .uri("/api/v1/countries")
                    .retrieve()
                    .bodyToFlux(CountryDTO.class)
                    .timeout(TIMEOUT)
                    .collectList()
                    .block();
            
            log.info("Obtenidos {} países en total", 
                    countries != null ? countries.size() : 0);
            
            return countries != null ? countries : Collections.emptyList();
            
        } catch (WebClientResponseException e) {
            log.error("Error HTTP al obtener todos los países: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al obtener países del Teams Service: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("Error inesperado al obtener países: {}", e.getMessage(), e);
            throw new RuntimeException("Error de comunicación con Teams Service", e);
        }
    }
    
    /**
     * Obtiene equipos/clubes disponibles según el tipo de torneo
     * @param tournamentType tipo de torneo
     * @return lista de equipos disponibles
     * @throws RuntimeException si hay error en la comunicación
     */
    public List<TeamDTO> getTeamsByTournamentType(TournamentType tournamentType) {
        try {
            log.info("Obteniendo equipos para tipo de torneo: {}", tournamentType);
            
            List<TeamDTO> teams = teamsWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/teams/tournament-type/{type}")
                            .build(tournamentType.name()))
                    .retrieve()
                    .bodyToFlux(TeamDTO.class)
                    .timeout(TIMEOUT)
                    .collectList()
                    .block();
            
            log.info("Obtenidos {} equipos para torneo {}", 
                    teams != null ? teams.size() : 0, tournamentType);
            
            return teams != null ? teams : Collections.emptyList();
            
        } catch (WebClientResponseException e) {
            log.error("Error HTTP al obtener equipos para {}: {} - {}", 
                    tournamentType, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al obtener equipos del Teams Service: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("Error inesperado al obtener equipos para {}: {}", tournamentType, e.getMessage(), e);
            throw new RuntimeException("Error de comunicación con Teams Service", e);
        }
    }
    
    /**
     * Obtiene todos los equipos disponibles
     * @return lista de todos los equipos
     * @throws RuntimeException si hay error en la comunicación
     */
    public List<TeamDTO> getAllTeams() {
        try {
            log.info("Obteniendo todos los equipos");
            
            List<TeamDTO> teams = teamsWebClient.get()
                    .uri("/api/v1/teams")
                    .retrieve()
                    .bodyToFlux(TeamDTO.class)
                    .timeout(TIMEOUT)
                    .collectList()
                    .block();
            
            log.info("Obtenidos {} equipos en total", 
                    teams != null ? teams.size() : 0);
            
            return teams != null ? teams : Collections.emptyList();
            
        } catch (WebClientResponseException e) {
            log.error("Error HTTP al obtener todos los equipos: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al obtener equipos del Teams Service: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("Error inesperado al obtener equipos: {}", e.getMessage(), e);
            throw new RuntimeException("Error de comunicación con Teams Service", e);
        }
    }
    
    /**
     * Obtiene países de un continente específico
     * @param continent nombre del continente
     * @return lista de países del continente
     * @throws RuntimeException si hay error en la comunicación
     */
    public List<CountryDTO> getCountriesByContinent(String continent) {
        if (continent == null || continent.trim().isEmpty()) {
            throw new IllegalArgumentException("El continente no puede ser nulo o vacío");
        }
        
        try {
            log.info("Obteniendo países del continente: {}", continent);
            
            List<CountryDTO> countries = teamsWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/countries/continent/{continent}")
                            .build(continent))
                    .retrieve()
                    .bodyToFlux(CountryDTO.class)
                    .timeout(TIMEOUT)
                    .collectList()
                    .block();
            
            log.info("Obtenidos {} países del continente {}", 
                    countries != null ? countries.size() : 0, continent);
            
            return countries != null ? countries : Collections.emptyList();
            
        } catch (WebClientResponseException e) {
            log.error("Error HTTP al obtener países de {}: {} - {}", 
                    continent, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al obtener países del Teams Service: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("Error inesperado al obtener países de {}: {}", continent, e.getMessage(), e);
            throw new RuntimeException("Error de comunicación con Teams Service", e);
        }
    }
    
    /**
     * Obtiene equipos de un continente específico
     * @param continent nombre del continente
     * @return lista de equipos del continente
     * @throws RuntimeException si hay error en la comunicación
     */
    public List<TeamDTO> getTeamsByContinent(String continent) {
        if (continent == null || continent.trim().isEmpty()) {
            throw new IllegalArgumentException("El continente no puede ser nulo o vacío");
        }
        
        try {
            log.info("Obteniendo equipos del continente: {}", continent);
            
            List<TeamDTO> teams = teamsWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/teams/continent/{continent}")
                            .build(continent))
                    .retrieve()
                    .bodyToFlux(TeamDTO.class)
                    .timeout(TIMEOUT)
                    .collectList()
                    .block();
            
            log.info("Obtenidos {} equipos del continente {}", 
                    teams != null ? teams.size() : 0, continent);
            
            return teams != null ? teams : Collections.emptyList();
            
        } catch (WebClientResponseException e) {
            log.error("Error HTTP al obtener equipos de {}: {} - {}", 
                    continent, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al obtener equipos del Teams Service: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("Error inesperado al obtener equipos de {}: {}", continent, e.getMessage(), e);
            throw new RuntimeException("Error de comunicación con Teams Service", e);
        }
    }
    
    /**
     * Obtiene información detallada de un país específico
     * @param countryId ID del país
     * @return datos del país
     * @throws RuntimeException si hay error en la comunicación
     */
    public CountryDTO getCountryById(Long countryId) {
        if (countryId == null) {
            throw new IllegalArgumentException("El ID del país no puede ser nulo");
        }
        
        try {
            log.info("Obteniendo información del país ID: {}", countryId);
            
            CountryDTO country = teamsWebClient.get()
                    .uri("/api/v1/countries/{id}", countryId)
                    .retrieve()
                    .bodyToMono(CountryDTO.class)
                    .timeout(TIMEOUT)
                    .block();
            
            if (country != null) {
                log.info("Obtenido país: {}", country.getName());
            } else {
                log.warn("País con ID {} no encontrado", countryId);
            }
            
            return country;
            
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.warn("País con ID {} no encontrado", countryId);
                return null;
            }
            log.error("Error HTTP al obtener país {}: {} - {}", 
                    countryId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al obtener país del Teams Service: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("Error inesperado al obtener país {}: {}", countryId, e.getMessage(), e);
            throw new RuntimeException("Error de comunicación con Teams Service", e);
        }
    }
    
    /**
     * Obtiene información detallada de un equipo específico
     * @param teamId ID del equipo
     * @return datos del equipo
     * @throws RuntimeException si hay error en la comunicación
     */
    public TeamDTO getTeamById(Long teamId) {
        if (teamId == null) {
            throw new IllegalArgumentException("El ID del equipo no puede ser nulo");
        }
        
        try {
            log.info("Obteniendo información del equipo ID: {}", teamId);
            
            TeamDTO team = teamsWebClient.get()
                    .uri("/api/v1/teams/{id}", teamId)
                    .retrieve()
                    .bodyToMono(TeamDTO.class)
                    .timeout(TIMEOUT)
                    .block();
            
            if (team != null) {
                log.info("Obtenido equipo: {}", team.getName());
            } else {
                log.warn("Equipo con ID {} no encontrado", teamId);
            }
            
            return team;
            
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.warn("Equipo con ID {} no encontrado", teamId);
                return null;
            }
            log.error("Error HTTP al obtener equipo {}: {} - {}", 
                    teamId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al obtener equipo del Teams Service: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("Error inesperado al obtener equipo {}: {}", teamId, e.getMessage(), e);
            throw new RuntimeException("Error de comunicación con Teams Service", e);
        }
    }
}