package com.pes.copa.tournaments.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;

/**
 * DTO para equipos disponibles que se pueden seleccionar
 * Versión simplificada para mostrar en la lista de selección
 */
@Data
@Builder
public class AvailableTeamDTO {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("logoURL")
    private String logoURL;
    
    @JsonProperty("country")
    private String country; // Solo para clubes
    
    @JsonProperty("continent")
    private String continent;
    
    @JsonProperty("isAlreadySelected")
    private Boolean isAlreadySelected; // Si ya está en el torneo
    
    @JsonProperty("teamType")
    private String teamType; // "COUNTRY" o "CLUB"
}