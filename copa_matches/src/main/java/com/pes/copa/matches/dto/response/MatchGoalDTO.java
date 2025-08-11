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
public class MatchGoalDTO {
    private String playerName;
    private Integer minute;
    private Boolean isOwnGoal;
    private String teamName;
}