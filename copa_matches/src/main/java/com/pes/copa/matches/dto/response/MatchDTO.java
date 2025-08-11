/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 *
 * @author sangr
 */
@Data
public class MatchDTO {
    private Long id;
    private Long tournamentId;
    private TeamBasicDTO homeTeam;
    private TeamBasicDTO awayTeam;
    private String phase;
    private String groupName;
    private Integer matchday;
    private Integer homeScore;
    private Integer awayScore;
    private Integer homePenalties;
    private Integer awayPenalties;
    private String status;
    private Boolean requiresPlayerInput;
    private LocalDateTime playedDate;
    private List<MatchGoalDTO> goals;
}