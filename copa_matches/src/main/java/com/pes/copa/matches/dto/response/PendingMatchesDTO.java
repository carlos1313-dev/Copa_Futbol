/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.dto.response;
/**
 *
 * @author sangr
 */
import lombok.Data;
import java.util.List;

@Data
public class PendingMatchesDTO {
    private Long playerId;
    private String playerName;
    private String playerUsername; // Del Auth Service
    private Integer totalPendingMatches;
    private List<MatchDTO> pendingMatches;
    
    // Agrupado por fase para mejor organización
    private List<MatchDTO> groupStageMatches;
    private List<MatchDTO> knockoutMatches;
}