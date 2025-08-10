package com.pes.copa.tournaments.repository;

import com.pes.copa.tournaments.entity.TournamentTeam;
import com.pes.copa.tournaments.enums.TournamentType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Long> {
    
    /**
     * Encuentra equipos por torneo, ordenados por grupo y posición
     */
    List<TournamentTeam> findByTournamentIdOrderByGroupNameAscPositionAsc(Long tournamentId);
    
    /**
     * Encuentra equipos por torneo
     */
    List<TournamentTeam> findByTournamentId(Long tournamentId);
    
    /**
     * Encuentra equipos por torneo y grupo
     */
    List<TournamentTeam> findByTournamentIdAndGroupName(Long tournamentId, String groupName);
    
    /**
     * Encuentra equipos por torneo y jugador
     */
    List<TournamentTeam> findByTournamentIdAndPlayerId(Long tournamentId, Long playerId);
    
    /**
     * Encuentra equipos activos (no eliminados)
     */
    List<TournamentTeam> findByTournamentIdAndIsEliminatedFalse(Long tournamentId);
    
    /**
     * Encuentra un equipo específico en un torneo
     */
    Optional<TournamentTeam> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);
    
    /**
     * Verifica si existe un equipo en una posición específica
     */
    boolean existsByTournamentIdAndGroupNameAndPosition(Long tournamentId, String groupName, Integer position);
    
    /**
     * Cuenta el total de equipos en un torneo
     */
    long countByTournamentId(Long tournamentId);
    
    /**
     * Cuenta jugadores únicos en un torneo
     */
    @Query("SELECT COUNT(DISTINCT tt.playerId) FROM TournamentTeam tt WHERE tt.tournamentId = :tournamentId AND tt.playerId IS NOT NULL")
    long countDistinctPlayersByTournamentId(@Param("tournamentId") Long tournamentId);
    
    /**
     * Elimina todos los equipos de un torneo
     */
    void deleteByTournamentId(Long tournamentId);
}