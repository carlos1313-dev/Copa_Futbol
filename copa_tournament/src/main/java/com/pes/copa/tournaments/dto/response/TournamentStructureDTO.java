package com.pes.copa.tournaments.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;
import java.util.List;
import java.util.Map;

/**
 * DTO que representa la estructura completa del torneo (grupos/bracket)
 * Se usa para mostrar el cuadrante completo en el frontend
 */
@Data
@Builder
public class TournamentStructureDTO {
    
    @JsonProperty("tournamentId")
    private Long tournamentId;
    
    @JsonProperty("tournamentName")
    private String tournamentName;
    
    @JsonProperty("format")
    private String format; // "GROUPS_THEN_KNOCKOUT" o "DIRECT_KNOCKOUT"
    
    @JsonProperty("groups")
    private Map<String, List<TeamPositionDTO>> groups; // Solo si hay fase de grupos
    
    @JsonProperty("bracket")
    private List<TeamPositionDTO> bracket; // Para eliminación directa o después de grupos
    
    @JsonProperty("totalPositions")
    private Integer totalPositions;
    
    @JsonProperty("filledPositions")
    private Integer filledPositions;
    
    @JsonProperty("isComplete")
    private Boolean isComplete; // Si todos los espacios están llenos
}