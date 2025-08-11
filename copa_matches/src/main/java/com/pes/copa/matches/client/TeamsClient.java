/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.client;

/**
 *
 * @author sangr
 */
import com.pes.copa.matches.dto.external.TeamBasicDTO;
import com.pes.copa.matches.dto.response.TeamBasicDTO;
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
     *  Obtiene información básica de un país con manejo de errores
     */
    public TeamBasicDTO getCountryBasicInfo(Long countryId) {
        try {
            String url = teamsServiceUrl + "/api/v1/countries/" + countryId + "/basic";
            return restTemplate.getForObject(url, TeamBasicDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to get country info: " + e.getMessage());
        }
    }
    
    /**
     *  Obtiene información básica de un equipo con manejo de errores
     */
    public TeamBasicDTO getTeamBasicInfo(Long teamId) {
        try {
            String url = teamsServiceUrl + "/api/v1/teams/" + teamId + "/basic";
            return restTemplate.getForObject(url, TeamBasicDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to get team info: " + e.getMessage());
        }
    }
}