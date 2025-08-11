/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 *
 * @author sangr
 */
@Data
@Entity
@Table(name = "match_goals")
public class MatchGoal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;
    
    @Column(name = "match_id", nullable = false)
    @JsonProperty("matchId")
    private Long matchId;
    
    @Column(name = "team_id", nullable = false)
    @JsonProperty("teamId")
    private Long teamId;
    
    @Column(name = "player_name", length = 100)
    @JsonProperty("playerName")
    private String playerName;
    
    @Column(name = "minute")
    @JsonProperty("minute")
    private Integer minute;
    
    @Column(name = "is_own_goal", nullable = false)
    @JsonProperty("isOwnGoal")
    private Boolean isOwnGoal = false;
}