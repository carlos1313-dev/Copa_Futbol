/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.client;
import com.pes.copa.matches.dto.external.CountryDTO;
import com.pes.copa.matches.dto.external.TeamDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Component
public class TeamsClient {
    
    @Value("${services.teams.url:http://localhost:8081}")
    private String teamsServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public TeamsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Obtiene información de un país/selección nacional
     */
    public CountryDTO getCountry(Long countryId) {
        try {
            String url = teamsServiceUrl + "/api/v1/countries/" + countryId;
            return restTemplate.getForObject(url, CountryDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to get country info: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene información de un equipo de club
     */
    public TeamDTO getTeam(Long teamId) {
        try {
            String url = teamsServiceUrl + "/api/v1/teams/" + teamId;
            return restTemplate.getForObject(url, TeamDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to get team info: " + e.getMessage());
        }
    }
}