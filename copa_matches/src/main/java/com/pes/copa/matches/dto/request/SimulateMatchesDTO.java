/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.dto.request;

import lombok.Data;

/**
 *
 * @author sangr
 */
@Data
public class SimulateMatchesDTO {
    private Long tournamentId;
    private String phase; // Opcional, simula solo una fase específica
    private String groupName; // Opcional, simula solo un grupo
}