/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.teams.repository;

import com.pes.copa.teams.entity.Country;
import com.pes.copa.teams.entity.Teams;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author sangr
 */
@Repository
public interface CountryRepository extends JpaRepository<Country, Long>{
    
    /**
     * Busca todas las selecciones de un continente
     */
    List<Country> findByContinent(String continent);
    
    /**
     * Busca todas las selecciones mundialistas
     */
    List<Country> findByIsMundialistTrue();
}