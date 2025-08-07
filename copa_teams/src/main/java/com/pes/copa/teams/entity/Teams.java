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
 * Entidad que representa un equipo de club
 * @author sangr
 */
@Data
@Entity
public class Teams {
    
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
    @JsonProperty("country")
    private String country;
    
    @Column(nullable = false)
    @Size(min = 2, max = 50)
    @JsonProperty("continent")
    private String continent;
    
    @Column(name = "logoURL", unique = true, nullable = false)    
    @JsonProperty("logoURL")
    private String logoURL;
    
    @Column(name = "isChampions", nullable = false)    
    @JsonProperty("isChampions") 
    private boolean isChampions;
    
}
