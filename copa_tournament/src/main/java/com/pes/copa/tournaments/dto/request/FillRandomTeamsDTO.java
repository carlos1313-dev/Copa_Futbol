package com.pes.copa.tournaments.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * DTO para llenar automáticamente las posiciones vacantes con equipos aleatorios
 */
@Data
public class FillRandomTeamsDTO {
    
    @JsonProperty("excludeTeamIds")
    private List<Long> excludeTeamIds; // IDs de equipos que NO se deben incluir
    
    @JsonProperty("onlyFromContinent")
    private String onlyFromContinent; // Si se quiere solo equipos de un continente específico
    
    @JsonProperty("includeAiPlayers")
    private Boolean includeAiPlayers = true; // Si los equipos random tendrán jugador IA o no
}