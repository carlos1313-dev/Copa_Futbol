/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.teams.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author sangr
 */
@Data
@Entity
public class Teams {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //El id es un valor autogenerado en la BD
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100) //Se crea una columna de valor único (no se puede repetir nombre), que no puede estar vacío
    @Size (min = 2, max = 50)
    private String name;
    
    @Column (nullable = false)
    @Size (min = 2, max = 50)
    private String country;
    
    @Column (nullable = false)
    @Size (min = 2, max = 50)
    private String continent;
    
    @Column(unique = true, nullable = false)    
    private String logo;
    
    @Column(unique = true, nullable = false)    
    private boolean isChampions;
    
}
