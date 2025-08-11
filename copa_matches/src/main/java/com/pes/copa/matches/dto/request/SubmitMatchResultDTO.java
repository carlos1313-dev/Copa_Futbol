/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.dto.request;

import com.pes.copa.matches.dto.request.GoalDTO;
import java.util.List;
import lombok.Data;

/**
 *
 * @author sangr
 */
@Data
public class SubmitMatchResultDTO {
    private Long matchId;
    private Integer homeScore;
    private Integer awayScore;
    private Integer homePenalties; // Opcional, solo para eliminatorias
    private Integer awayPenalties;
    private List<GoalDTO> goals;
}