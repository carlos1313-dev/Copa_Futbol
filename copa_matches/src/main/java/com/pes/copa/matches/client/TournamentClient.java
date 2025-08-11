/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.client;

import com.pes.copa.matches.dto.external.TournamentTeamDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author sangr
 */
@Component
public class TournamentClient {
    
    @Value("${services.tournament.url:http://localhost:8082}")
    private String tournamentServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public TournamentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Obtiene información del torneo
     */
    public TournamentDTO getTournament(Long tournamentId) {
        String url = tournamentServiceUrl + "/api/v1/tournaments/" + tournamentId;
        return restTemplate.getForObject(url, TournamentDTO.class);
    }
    
    /**
     * Obtiene equipos del torneo
     */
    public List<TournamentTeamDTO> getTournamentTeams(Long tournamentId) {
        String url = tournamentServiceUrl + "/api/v1/tournaments/" + tournamentId + "/teams";
        return restTemplate.exchange(
            url, HttpMethod.GET, null, 
            new ParameterizedTypeReference<List<TournamentTeamDTO>>() {}
        ).getBody();
    }
    
    /**
     * Actualiza estado del torneo
     */
    public void updateTournamentStatus(Long tournamentId, String status) {
        String url = tournamentServiceUrl + "/api/v1/tournaments/" + tournamentId + "/status?status=" + status;
        restTemplate.put(url, null);
    }
}