/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.pes.copa.matches.enums;

/**
 *
 * @author sangr
 */
public enum MatchStatus {
    PENDING,        // Partido por jugarse
    IN_PROGRESS,    // En progreso (para futuras funcionalidades)
    FINISHED,       // Partido finalizado con resultado manual
    SIMULATED       // Partido simulado por IA
}
