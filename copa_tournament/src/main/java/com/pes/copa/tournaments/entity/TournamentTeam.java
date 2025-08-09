package com.pes.copa.tournaments.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pes.copa.tournaments.enums.TeamType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tournament_teams")
public class TournamentTeam {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;
    
    @Column(name = "tournament_id", nullable = false)
    @JsonProperty("tournamentId")
    private Long tournamentId;
    
    @Column(name = "team_id", nullable = false)
    @JsonProperty("teamId")
    private Long teamId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "team_type", nullable = false, length = 20)
    @JsonProperty("teamType")
    private TeamType teamType; // COUNTRY o CLUB
    
    @Column(name = "player_id")
    @JsonProperty("playerId")
    private Long playerId; // null si es IA
    
    @Column(name = "group_name", length = 5)
    @JsonProperty("groupName")
    private String groupName; // "A", "B", "C", etc. Para fase de grupos
    
    @Column(name = "is_eliminated", nullable = false)
    @JsonProperty("isEliminated")
    private Boolean isEliminated = false;
    
    @Column(name = "position", length = 5)
    @JsonProperty("position")
    private Integer position; 
}