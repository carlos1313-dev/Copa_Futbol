package com.pes.copa.teams.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Entidad que representa un país/selección nacional
 * @author sangr
 */
@Data
@Entity
public class Country {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    @Size(min = 2, max = 50)
    @JsonProperty("name")
    private String name;
    
    @Column(nullable = false)
    @Size(min = 2, max = 50)
    @JsonProperty("continent")
    private String continent;
    
    @Column(name = "FlagURL",unique = true, nullable = false)    
    @JsonProperty("FlagURL")
    private String FlagURL;
    
    @Column(name = "isMundialist", nullable = false)
    @JsonProperty("isMundialist") // Nombre más claro para el frontend
    private boolean isMundialist;
    
}