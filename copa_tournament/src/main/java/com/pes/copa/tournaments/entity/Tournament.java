package com.pes.copa.tournaments.entity;

import com.pes.copa.tournaments.enums.TournamentType;
import com.pes.copa.tournaments.enums.TournamentStatus;
import com.pes.copa.tournaments.enums.TournamentFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tournaments")
public class Tournament {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;
    
    @Column(nullable = false, length = 100)
    @Size(min = 2, max = 100)
    @JsonProperty("name")
    private String name;
    
    @Enumerated(EnumType.STRING) // Guarda el enum como string
    @Column(name = "tournament_type", nullable = false, length = 50)
    @JsonProperty("tournamentType")
    private TournamentType tournamentType; //Mundial, Champions, Personalizado...
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tournament_format", nullable = false, length = 50)
    @JsonProperty("tournamentFormat")
    private TournamentFormat tournamentFormat; // Primero fase de grupos y luego eliminatoria / Eliminatoria directamente
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @JsonProperty("status")
    private TournamentStatus status; //No iniciado, En proceso, Finalizado
    
    @Column(name = "num_teams", nullable = false)
    @JsonProperty("numTeams")
    private Integer numTeams;
    
    @Column(name = "num_players", nullable = false)
    @JsonProperty("numPlayers")
    private Integer numPlayers = 0;
    
    @Column(name = "created_date", nullable = false)
    @JsonProperty("createdDate")
    private LocalDateTime createdDate;
    
    @Column(name = "creator_user_id", nullable = false)
    @JsonProperty("creatorUserId")
    private Long creatorUserId;
}