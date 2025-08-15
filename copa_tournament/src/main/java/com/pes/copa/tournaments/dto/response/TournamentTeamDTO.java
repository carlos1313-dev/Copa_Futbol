/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.tournaments.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pes.copa.tournaments.dto.external.CountryDTO;
import com.pes.copa.tournaments.dto.external.TeamDTO;
import com.pes.copa.tournaments.enums.TeamType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author sangr
 */
@Data
@Builder

public class TournamentTeamDTO {
    
    @JsonProperty("id")
    private Long id;

    @JsonProperty("tournamentId")
    private Long tournamentId;

    @JsonProperty("teamId")
    private Long teamId;
    
    @Enumerated(EnumType.STRING)
    @JsonProperty("teamType")
    private TeamType teamType; // COUNTRY o CLUB

    private TeamDTO team; //Si es Team
    private CountryDTO country;// Si es country
}
