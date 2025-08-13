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

@Data
public class TeamPositionDTO {
    private Long teamId;
    private String teamName; // Viene del Teams service
    private String logoURL; // Viene del Teams service
    private String country; // Solo para equipos de club
    private Long playerId;
    private String playerName; // Viene del Auth service
    private Integer position;
    private String groupName; // "A", "B", etc. Solo para torneos con grupos
    private Boolean isEliminated;
    private Boolean isEmpty;
    private Boolean isAI; // true si playerId es null (controlado por IA)
    private String teamType; // "COUNTRY" o "CLUB"
}