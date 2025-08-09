package com.pes.copa.tournaments.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para asignar un equipo a una posición específica en el torneo
 * Se usa cuando el jugador arrastra un equipo a una posición del bracket/grupo
 */
@Data
public class AssignTeamDTO {
    
    @NotNull(message = "El ID del equipo es obligatorio")
    @JsonProperty("teamId")
    private Long teamId;
    
    @JsonProperty("playerId")
    private Long playerId; // null si el equipo será controlado por IA
    
    @JsonProperty("groupName")
    private String groupName; // "A", "B", "C", etc. Solo para torneos con grupos
    
    @JsonProperty("position")
    private Integer position; // Posición específica dentro del grupo o bracket
}