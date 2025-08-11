/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.repository;

import com.pes.copa.matches.entity.Match;
import com.pes.copa.matches.enums.MatchPhase;
import com.pes.copa.matches.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    // ✅ Consultas que solo usan datos locales
    List<Match> findByTournamentId(Long tournamentId);
    
    List<Match> findByTournamentIdAndPhase(Long tournamentId, MatchPhase phase);
    
    List<Match> findByTournamentIdAndGroupName(Long tournamentId, String groupName);
    
    List<Match> findByTournamentIdAndStatus(Long tournamentId, MatchStatus status);
    
    List<Match> findByTournamentIdAndPhaseAndStatus(Long tournamentId, MatchPhase phase, MatchStatus status);
    
    List<Match> findByTournamentIdAndGroupNameAndMatchday(Long tournamentId, String groupName, Integer matchday);
    
    @Query("SELECT DISTINCT m.groupName FROM Match m WHERE m.tournamentId = :tournamentId AND m.phase = 'GROUP_STAGE'")
    List<String> findDistinctGroupNamesByTournamentId(@Param("tournamentId") Long tournamentId);
    
    // ✅ Consultas por equipos específicos (datos locales)
    @Query("SELECT m FROM Match m WHERE m.tournamentId = :tournamentId AND " +
           "(m.homeTeamId = :teamId OR m.awayTeamId = :teamId) AND m.status = 'PENDING'")
    List<Match> findPendingMatchesByTeam(@Param("tournamentId") Long tournamentId, @Param("teamId") Long teamId);
    
    @Query("SELECT m FROM Match m WHERE m.tournamentId = :tournamentId AND " +
           "m.requiresPlayerInput = true AND m.status = 'PENDING'")
    List<Match> findPendingPlayerMatches(@Param("tournamentId") Long tournamentId);
}