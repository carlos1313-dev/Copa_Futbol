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
import java.time.LocalDateTime;

@Data
public class TournamentDTO {
    private Long id;
    private String name;
    private String tournamentType; // WORLD_CUP, COPA_AMERICA, etc.
    private String tournamentFormat; // GROUPS_THEN_KNOCKOUT, DIRECT_KNOCKOUT
    private String status; // CREATED, IN_PROGRESS, FINISHED
    private Integer availableSlots;
    private Integer numPlayers;
    private Integer numTeams;
    private LocalDateTime createdDate;
    private Long creatorUserId;
    private Boolean canJoin;
    private Boolean isStarted;
}
