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
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MatchDTO {
    private Long id;
    private Long tournamentId;
    private TeamBasicDTO homeTeam;
    private TeamBasicDTO awayTeam;
    private String phase; // GROUP_STAGE, QUARTER_FINAL, etc.
    private String groupName; // Solo para fase de grupos
    private Integer matchday; // 1, 2, 3 para fase de grupos
    private Integer homeScore;
    private Integer awayScore;
    private Integer homePenalties; // Solo para eliminatorias
    private Integer awayPenalties;
    private String status; // PENDING, FINISHED, etc.
    private Boolean requiresPlayerInput; // true si algún equipo tiene jugador humano
    private LocalDateTime playedDate;
    private List<MatchGoalDTO> goals;
    private Long nextMatchId; // Para eliminatorias: partido al que avanza el ganador
    private String winnerAdvancesTo; // Descripción legible: "Cuartos de Final", etc.
}