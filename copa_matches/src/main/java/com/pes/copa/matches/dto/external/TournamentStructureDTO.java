/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.dto.external;

/**
 *
 * @author sangr
 */

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class TournamentStructureDTO {
    private Long tournamentId;
    private String tournamentName;
    private String format; // "GROUPS_THEN_KNOCKOUT" o "DIRECT_KNOCKOUT"
    private Map<String, List<TeamPositionDTO>> groups; // Solo si hay fase de grupos
    private List<TeamPositionDTO> bracket; // Para eliminación directa o después de grupos
    private Integer totalPositions;
    private Integer filledPositions;
    private Boolean isComplete;
}