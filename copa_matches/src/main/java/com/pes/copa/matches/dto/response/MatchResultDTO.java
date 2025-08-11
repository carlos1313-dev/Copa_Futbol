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

@Data
public class MatchResultDTO {
    private Long matchId;
    private Long tournamentId;
    private Long homeTeamId;
    private Long awayTeamId;
    private Integer homeScore;
    private Integer awayScore;
    private String phase;
    private String groupName;
    private Long homePlayerId;
    private Long awayPlayerId;
}