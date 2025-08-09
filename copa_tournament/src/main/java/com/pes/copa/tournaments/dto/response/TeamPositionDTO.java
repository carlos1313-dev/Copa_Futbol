package com.pes.copa.tournaments.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;

/**
 * DTO que representa un equipo en una posición específica del torneo
 * Combina datos del equipo (de Teams service) con su posición en el torneo
 */
@Data
@Builder
public class TeamPositionDTO {
    
    @JsonProperty("teamId")
    private Long teamId;
    
    @JsonProperty("teamName")
    private String teamName; // Viene del Teams service
    
    @JsonProperty("logoURL")
    private String logoURL; // Viene del Teams service
    
    @JsonProperty("country")
    private String country; // Solo para equipos de club
    
    @JsonProperty("playerId")
    private Long playerId;
    
    @JsonProperty("playerName")
    private String playerName; // Viene del Auth service
    
    @JsonProperty("position")
    private Integer position;
    
    @JsonProperty("groupName")
    private String groupName; // "A", "B", etc. Solo para torneos con grupos
    
    @JsonProperty("isEliminated")
    private Boolean isEliminated;
    
    @JsonProperty("isEmpty")
    private Boolean isEmpty; // true si esta posición no tiene equipo asignado
    
    @JsonProperty("isAI")
    private Boolean isAI; // true si playerId es null (controlado por IA)
    
    @JsonProperty("teamType")
    private String teamType; // "COUNTRY" o "CLUB"
}