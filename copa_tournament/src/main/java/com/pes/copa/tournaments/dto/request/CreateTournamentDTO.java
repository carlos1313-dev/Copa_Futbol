package com.pes.copa.tournaments.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pes.copa.tournaments.enums.TournamentFormat;
import com.pes.copa.tournaments.enums.TournamentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para crear un nuevo torneo
 * Recibe los datos básicos del frontend para configurar el torneo
 */
@Data
public class CreateTournamentDTO {
    
    @NotNull(message = "El nombre del torneo es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @JsonProperty("name")
    private String name;
    
    @NotNull(message = "El tipo de torneo es obligatorio")
    @JsonProperty("tournamentType")
    private TournamentType tournamentType; // WORLD_CUP, COPA_AMERICA, etc.
    
    @NotNull(message = "El formato de torneo es obligatorio")
    @JsonProperty("tournamentFormat")
    private TournamentFormat tournamentFormat; // GROUPS_THEN_KNOCKOUT, DIRECT_KNOCKOUT
    
    @NotNull(message = "El número máximo de jugadores es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 jugador")
    @Max(value = 32, message = "Máximo 32 jugadores")
    @JsonProperty("numPlayers")
    private Integer numPlayers;
    
    @NotNull(message = "El número total de equipos es obligatorio")
    @Min(value = 4, message = "Debe haber al menos 4 equipos")
    @Max(value = 32, message = "Máximo 32 equipos")
    @JsonProperty("numTeams")
    private Integer numTeams;
    
    @NotNull(message = "El ID del creador es obligatorio")
    @JsonProperty("creatorUserId")
    private Long creatorUserId;
}