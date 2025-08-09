/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.tournaments.dto.external;

import lombok.Data;

/**
 *
 * @author sangr
 */
@Data
public class CountryDTO {
    private Long id;
    private String name;
    private String continent;
    private String FlagURL;
    private boolean isMundialist;
}
