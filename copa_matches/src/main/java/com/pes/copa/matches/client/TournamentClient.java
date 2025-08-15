package com.pes.copa.matches.client;

import com.pes.copa.matches.dto.external.TournamentDTO;
import com.pes.copa.matches.dto.external.TeamPositionDTO;
import com.pes.copa.matches.dto.external.TournamentStructureDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TournamentClient {
    
    @Value("${services.tournament.url:http://localhost:8080}")
    private String tournamentServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public TournamentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Obtiene información básica del torneo
     */
    public TournamentDTO getTournament(Long tournamentId) {
        String url = tournamentServiceUrl + "/api/v1/tournaments/" + tournamentId;
        return restTemplate.getForObject(url, TournamentDTO.class);
    }
    
    /**
     * Obtiene la estructura completa del torneo (equipos y posiciones)
     */
    public TournamentStructureDTO getTournamentStructure(Long tournamentId) {
        String url = tournamentServiceUrl + "/api/v1/tournaments/" + tournamentId + "/structure";
        return restTemplate.getForObject(url, TournamentStructureDTO.class);
    }
    
    /**
     * Obtiene equipos de un jugador específico en el torneo
     */
    public List<TeamPositionDTO> getPlayerTeams(Long tournamentId, Long playerId) {
        String url = tournamentServiceUrl + "/api/v1/tournaments/" + tournamentId + "/players/" + playerId + "/teams";
        return restTemplate.exchange(
            url, HttpMethod.GET, null,
            new ParameterizedTypeReference<List<TeamPositionDTO>>() {}
        ).getBody();
    }
    
    /**
     * Obtiene todos los equipos del torneo
     */
    public List<TeamPositionDTO> getTournamentTeams(Long tournamentId) {
        String url = tournamentServiceUrl + "/api/v1/tournaments/" + tournamentId + "/teams";
        return restTemplate.exchange(
            url, HttpMethod.GET, null,
            new ParameterizedTypeReference<List<TeamPositionDTO>>() {}
        ).getBody();
    }
    
    /**
     * Actualiza el estado del torneo
     */
    public void updateTournamentStatus(Long tournamentId, String status) {
        String url = tournamentServiceUrl + "/api/v1/tournaments/" + tournamentId + "/status?status=" + status;
        restTemplate.put(url, null);
    }
}