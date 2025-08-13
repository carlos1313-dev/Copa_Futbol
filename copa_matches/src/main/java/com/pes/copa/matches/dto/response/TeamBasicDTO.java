/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.dto.response;

import lombok.Data;
/**
 *
 * @author sangr
 */
@Data
public class TeamBasicDTO {
    private Long id;
    private String name;
    private String logoUrl; 
    private String flagUrl;  // Para países
    private String country;  // Solo para clubes
    private String continent;
    private String teamType; // "COUNTRY" o "CLUB"
    
    // Información del contexto del torneo
    private Long playerId; // null si es IA
    private String playerName; // null si es IA
    private Boolean isAI;
    
    // Información específica del equipo
    private Boolean isMundialist; // Solo para países
    private Boolean isChampions;  // Solo para clubes
}