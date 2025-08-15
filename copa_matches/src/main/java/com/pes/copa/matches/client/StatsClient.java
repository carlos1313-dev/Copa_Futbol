/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.client;

import com.pes.copa.matches.dto.external.TeamPositionDTO;
import com.pes.copa.matches.dto.response.MatchResultDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author sangr
 */
@Component
public class StatsClient {
    
    @Value("${services.stats.url:http://localhost:8084}")
    private String statsServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public StatsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     *  Notifica resultado de partido (llamada asíncrona)
     */
    public void processMatchResult(MatchResultDTO matchResult) {
        try {
            String url = statsServiceUrl + "/api/v1/stats/matches/result";
            restTemplate.postForObject(url, matchResult, Void.class);
        } catch (RestClientException e) {
            // ✅ No fallar la operación principal si Stats Service falla
            System.err.println("Failed to notify stats service: " + e.getMessage());
        }
    }
    
    /**
     *  Inicializa estadísticas de torneo
     */
    public void initializeTournamentStats(Long tournamentId, List<TeamPositionDTO> teams) {
        try {
            String url = statsServiceUrl + "/api/v1/stats/tournaments/" + tournamentId + "/initialize";
            restTemplate.postForObject(url, teams, Void.class);
        } catch (RestClientException e) {
            System.err.println("Failed to initialize tournament stats: " + e.getMessage());
        }
    }
}