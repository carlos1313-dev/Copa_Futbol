package com.pes.copa.matches.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pes.copa.matches.enums.MatchPhase;
import com.pes.copa.matches.enums.MatchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Entidad que representa un equipo de club
 * @author sangr
 */
@Data
@Entity
@Table(name = "matches")
public class Match {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;
    
    @Column(name = "tournament_id", nullable = false)
    @JsonProperty("tournamentId")
    private Long tournamentId;
    
    @Column(name = "home_team_id", nullable = false)
    @JsonProperty("homeTeamId")
    private Long homeTeamId;
    
    @Column(name = "away_team_id", nullable = false)
    @JsonProperty("awayTeamId")
    private Long awayTeamId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "phase", nullable = false, length = 20)
    @JsonProperty("phase")
    private MatchPhase phase; // GROUP_STAGE, ROUND_16, QUARTER_FINAL, etc.
    
    @Column(name = "group_name", length = 5)
    @JsonProperty("groupName")
    private String groupName; // Solo para fase de grupos
    
    @Column(name = "matchday")
    @JsonProperty("matchday")
    private Integer matchday; // Para fase de grupos (1, 2, 3)
    
    @Column(name = "home_score")
    @JsonProperty("homeScore")
    private Integer homeScore;
    
    @Column(name = "away_score")
    @JsonProperty("awayScore")
    private Integer awayScore;
    
    @Column(name = "home_penalties")
    @JsonProperty("homePenalties")
    private Integer homePenalties; // Solo para eliminatorias
    
    @Column(name = "away_penalties")
    @JsonProperty("awayPenalties")
    private Integer awayPenalties;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @JsonProperty("status")
    private MatchStatus status; // PENDING, IN_PROGRESS, FINISHED, SIMULATED
    
    @Column(name = "requires_player_input", nullable = false)
    @JsonProperty("requiresPlayerInput")
    private Boolean requiresPlayerInput = false; // true si algún equipo tiene jugador humano
    
    @Column(name = "played_date")
    @JsonProperty("playedDate")
    private LocalDateTime playedDate;
    
    @Column(name = "next_match_id")
    @JsonProperty("nextMatchId")
    private Long nextMatchId; // Para eliminatorias, el partido al que avanza el ganador
}
