/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

/**
 *
 * @author sangr
 */

//Clase para enviar al Stats Service
@Data
public class MatchResultDTO {
    // Datos básicos del partido
    private Long matchId;
    private Long tournamentId;
    private Long homeTeamId;
    private Long awayTeamId;
    private String homeTeamName;
    private String awayTeamName;
    
    // Resultado
    private Integer homeScore;
    private Integer awayScore;
    private Integer homePenalties;
    private Integer awayPenalties;
    
    // Contexto del partido
    private String phase;
    private String groupName;
    private Integer matchday;
    
    // Información de jugadores (para estadísticas por jugador)
    private Long homePlayerId; // null si es IA
    private Long awayPlayerId; // null si es IA
    private String homePlayerName;
    private String awayPlayerName;
    
    // Metadatos
    private LocalDateTime playedDate;
    private Boolean wasSimulated;
    
    // Para determinar ganador en eliminatorias
    private Long winnerTeamId;
    private Long loserTeamId;
    private Boolean wasDecidedByPenalties;
}