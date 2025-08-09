package com.pes.copa.tournaments.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * DTO básico de torneo para enviar al frontend
 * Información general sin detalles de equipos
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
    
    //@JsonProperty("maxPlayers")
    //private Integer maxPlayers;
    
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