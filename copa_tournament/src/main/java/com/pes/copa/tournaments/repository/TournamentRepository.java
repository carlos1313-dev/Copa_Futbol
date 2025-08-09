package com.pes.copa.tournaments.repository;

import com.pes.copa.tournaments.entity.Tournament;
import com.pes.copa.tournaments.enums.TournamentStatus;
import com.pes.copa.tournaments.enums.TournamentType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    /**
     * Busca torneos por estado
     */
    List<Tournament> findByStatus(TournamentStatus status);

    /**
     * Busca torneos por tipo
     */
    List<Tournament> findByTournamentType(TournamentType tournamentType);

    /**
     * Busca torneos creados por un usuario específico
     */
    List<Tournament> findByCreatorUserIdOrderByCreatedDateDesc(Long creatorUserId);

    /**
     * Busca torneos por estado y tipo
     */
    List<Tournament> findByStatusAndTournamentType(TournamentStatus status, TournamentType tournamentType);

    /**
     * Busca torneos disponibles para unirse (actualizado)
     */
    @Query("SELECT t FROM Tournament t WHERE t.status = :status AND t.numPlayers < t.numTeams")
    List<Tournament> findAvailableToJoin(@Param("status") TournamentStatus status);
}
