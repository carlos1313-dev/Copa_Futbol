package com.pes.copa.tournaments.repository;

import com.pes.copa.tournaments.entity.TournamentTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de TournamentTeam
 * @author sangr
 */
@Repository
public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Long> {
    
    /**
     * Busca todos los equipos de un torneo ordenados por grupo y posición
     * @param tournamentId ID del torneo
     * @return lista de equipos ordenados
     */
    List<TournamentTeam> findByTournamentIdOrderByGroupNameAscPositionAsc(Long tournamentId);
    
    /**
     * Busca todos los equipos de un torneo sin orden específico
     * @param tournamentId ID del torneo
     * @return lista de equipos
     */
    List<TournamentTeam> findByTournamentId(Long tournamentId);
    
    /**
     * Busca un equipo específico en un torneo
     * @param tournamentId ID del torneo
     * @param teamId ID del equipo
     * @return equipo si existe
     */
    Optional<TournamentTeam> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);
    
    /**
     * Busca equipos de un grupo específico en un torneo
     * @param tournamentId ID del torneo
     * @param groupName nombre del grupo
     * @return lista de equipos del grupo
     */
    List<TournamentTeam> findByTournamentIdAndGroupName(Long tournamentId, String groupName);
    
    /**
     * Busca equipos controlados por un jugador específico en un torneo
     * @param tournamentId ID del torneo
     * @param playerId ID del jugador
     * @return lista de equipos del jugador
     */
    List<TournamentTeam> findByTournamentIdAndPlayerId(Long tournamentId, Long playerId);
    
    /**
     * Busca equipos que no han sido eliminados en un torneo
     * @param tournamentId ID del torneo
     * @return lista de equipos activos
     */
    List<TournamentTeam> findByTournamentIdAndIsEliminatedFalse(Long tournamentId);
    
    /**
     * Busca equipos controlados por IA (sin jugador asignado)
     * @param tournamentId ID del torneo
     * @return lista de equipos de IA
     */
    List<TournamentTeam> findByTournamentIdAndPlayerIdIsNull(Long tournamentId);
    
    /**
     * Cuenta el número total de equipos en un torneo
     * @param tournamentId ID del torneo
     * @return número de equipos
     */
    @Query("SELECT COUNT(tt) FROM TournamentTeam tt WHERE tt.tournamentId = :tournamentId")
    Long countByTournamentId(@Param("tournamentId") Long tournamentId);
    
    /**
     * Cuenta el número de jugadores únicos en un torneo (excluyendo IA)
     * @param tournamentId ID del torneo
     * @return número de jugadores únicos
     */
    @Query("SELECT COUNT(DISTINCT tt.playerId) FROM TournamentTeam tt WHERE tt.tournamentId = :tournamentId AND tt.playerId IS NOT NULL")
    Long countDistinctPlayersByTournamentId(@Param("tournamentId") Long tournamentId);
    
    /**
     * Busca equipos de un torneo en posiciones específicas
     * @param tournamentId ID del torneo
     * @param positions lista de posiciones
     * @return lista de equipos en esas posiciones
     */
    @Query("SELECT tt FROM TournamentTeam tt WHERE tt.tournamentId = :tournamentId AND tt.position IN :positions")
    List<TournamentTeam> findByTournamentIdAndPositionIn(@Param("tournamentId") Long tournamentId, 
                                                        @Param("positions") List<Integer> positions);
    
    /**
     * Busca la posición más alta ocupada en un torneo
     * @param tournamentId ID del torneo
     * @return posición máxima
     */
    @Query("SELECT MAX(tt.position) FROM TournamentTeam tt WHERE tt.tournamentId = :tournamentId AND tt.position IS NOT NULL")
    Optional<Integer> findMaxPositionByTournamentId(@Param("tournamentId") Long tournamentId);
    
    /**
     * Busca posiciones ocupadas en un grupo específico
     * @param tournamentId ID del torneo
     * @param groupName nombre del grupo
     * @return lista de posiciones ocupadas
     */
    @Query("SELECT tt.position FROM TournamentTeam tt WHERE tt.tournamentId = :tournamentId AND tt.groupName = :groupName AND tt.position IS NOT NULL ORDER BY tt.position")
    List<Integer> findOccupiedPositionsByTournamentIdAndGroup(@Param("tournamentId") Long tournamentId, 
                                                            @Param("groupName") String groupName);
    
    /**
     * Verifica si existe un equipo en una posición específica
     * @param tournamentId ID del torneo
     * @param position posición
     * @return true si la posición está ocupada
     */
    //@Query("SELECT CASE WHEN COUNT(tt) > 0 THEN true ELSE false END FROM TournamentTeam tt WHERE tt.tournamentId = :tournamentId AND tt.position = :position")
    //boolean existsByTournamentIdAndPosition(@Param("tournamentId") Long tournamentId, 
                                        //  @Param("position") Integer position);
    /**
     * Verifica si existe un equipo en una posición específica
     */
    boolean existsByTournamentIdAndGroupNameAndPosition(Long tournamentId, String groupName, Integer position);
    
    /**
     * Busca equipos de un torneo por tipo de equipo
     * @param tournamentId ID del torneo
     * @param teamType tipo de equipo (COUNTRY o CLUB)
     * @return lista de equipos del tipo especificado
     */
    @Query("SELECT tt FROM TournamentTeam tt WHERE tt.tournamentId = :tournamentId AND tt.teamType = :teamType")
    List<TournamentTeam> findByTournamentIdAndTeamType(@Param("tournamentId") Long tournamentId, 
                                                      @Param("teamType") com.pes.copa.tournaments.enums.TeamType teamType);
    
    /**
     * Elimina todos los equipos de un torneo
     * @param tournamentId ID del torneo
     * @return número de equipos eliminados
     */
    @Query("DELETE FROM TournamentTeam tt WHERE tt.tournamentId = :tournamentId")
    int deleteByTournamentId(@Param("tournamentId") Long tournamentId);
    
    /**
     * Busca grupos únicos de un torneo
     * @param tournamentId ID del torneo
     * @return lista de nombres de grupos
     */
    @Query("SELECT DISTINCT tt.groupName FROM TournamentTeam tt WHERE tt.tournamentId = :tournamentId AND tt.groupName IS NOT NULL ORDER BY tt.groupName")
    List<String> findDistinctGroupNamesByTournamentId(@Param("tournamentId") Long tournamentId);
}