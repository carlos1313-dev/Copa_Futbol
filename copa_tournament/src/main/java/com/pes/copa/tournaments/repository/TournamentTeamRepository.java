package com.pes.copa.tournaments.repository;

import com.pes.copa.tournaments.entity.TournamentTeam;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Long> {
    
    /**
     * Busca todos los equipos de un torneo
     */
    List<TournamentTeam> findByTournamentIdOrderByGroupNameAscPositionAsc(Long tournamentId);
    
    /**
     * Busca equipos de un torneo por grupo
     */
    List<TournamentTeam> findByTournamentIdAndGroupName(Long tournamentId, String groupName);
    
    /**
     * Busca equipos de un torneo controlados por un jugador específico
     */
    List<TournamentTeam> findByTournamentIdAndPlayerId(Long tournamentId, Long playerId);
    
    /**
     * Busca si un equipo específico ya está en el torneo
     */
    Optional<TournamentTeam> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);
    
    /**
     * Busca equipos sin jugador asignado (IA)
     */
    List<TournamentTeam> findByTournamentIdAndPlayerIdIsNull(Long tournamentId);
    
    /**
     * Busca equipos no eliminados de un torneo
     */
    List<TournamentTeam> findByTournamentIdAndIsEliminatedFalse(Long tournamentId);
    
    /**
     * Cuenta cuántos equipos tiene un torneo
     */
    long countByTournamentId(Long tournamentId);
    
    /**
     * Cuenta cuántos jugadores únicos tiene un torneo
     */
    @Query("SELECT COUNT(DISTINCT tt.playerId) FROM TournamentTeam tt WHERE tt.tournamentId = :tournamentId AND tt.playerId IS NOT NULL")
    long countDistinctPlayersByTournamentId(@Param("tournamentId") Long tournamentId);
    
    /**
     * Busca posiciones ocupadas en un grupo específico
     */
    @Query("SELECT tt.position FROM TournamentTeam tt WHERE tt.tournamentId = :tournamentId AND tt.groupName = :groupName")
    List<Integer> findOccupiedPositionsByTournamentIdAndGroup(@Param("tournamentId") Long tournamentId, 
                                                             @Param("groupName") String groupName);
    
    /**
     * Elimina todos los equipos de un torneo (para reiniciar)
     */
    void deleteByTournamentId(Long tournamentId);
}