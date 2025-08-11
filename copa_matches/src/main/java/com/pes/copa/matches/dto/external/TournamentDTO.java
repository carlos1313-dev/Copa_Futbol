/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author sangr
 */
@Data
@Builder
public class TournamentDTO {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("tournamentType")
    private String tournamentType; // String para mostrar nombre legible
    
    @JsonProperty("tournamentFormat")
    private String tournamentFormat; // String para mostrar nombre legible
    
    @JsonProperty("status")
    private String status; // String para mostrar nombre legible
    
    @JsonProperty("availableSlots")
    private Integer availableSlots;
    
    @JsonProperty("numPlayers")
    private Integer numPlayers;
    
    @JsonProperty("numTeams")
    private Integer numTeams;
    
    @JsonProperty("createdDate")
    private LocalDateTime createdDate;
    
    @JsonProperty("creatorUserId")
    private Long creatorUserId;
    
    @JsonProperty("canJoin")
    private Boolean canJoin; // Calculado: si el torneo acepta más jugadores
    
    @JsonProperty("isStarted")
    private Boolean isStarted; // Calculado: si el torneo ya comenzó
}
