/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.repository;
/**
 *
 * @author sangr
 */
import com.pes.copa.matches.entity.MatchGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatchGoalRepository extends JpaRepository<MatchGoal, Long> {
    
    List<MatchGoal> findByMatchId(Long matchId);
    
    List<MatchGoal> findByMatchIdOrderByMinute(Long matchId);
    
    void deleteByMatchId(Long matchId);
}