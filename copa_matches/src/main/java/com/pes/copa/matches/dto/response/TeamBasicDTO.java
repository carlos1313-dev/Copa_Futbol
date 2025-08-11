/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.dto.response;

import lombok.Data;

/**
 *
 * @author sangr
 */
@Data
public class TeamBasicDTO {
    private Long id;
    private String name;
    private String logoUrl;
    private Long playerId; // null si es IA
    private String playerName; // null si es IA
}