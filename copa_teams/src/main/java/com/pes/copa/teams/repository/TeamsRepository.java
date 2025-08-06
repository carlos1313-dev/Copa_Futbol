/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.teams.repository;

import com.pes.copa.teams.entity.Teams;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author sangr
 */
@Repository
public interface TeamsRepository extends JpaRepository<Teams, Long>{
    
    /**
     * Busca todos los equipos de un continente en específico
     */
    List<Teams> findByContinent(String continent);
    
    /**
     * Busca todas las selecciones de un continente
     */
    List<Teams> findByCountry(String country);
    
    /**
     * Busca todos los equipos clasificados a champions
     */
    List<Teams> findByIsChampionsTrue();

}
