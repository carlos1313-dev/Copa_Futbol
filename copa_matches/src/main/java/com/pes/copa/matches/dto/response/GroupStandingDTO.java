/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.dto.response;

import java.util.List;
import lombok.Data;

/**
 *
 * @author sangr
 */
@Data
public class GroupStandingDTO {
    private String groupName;
    private List<TeamStandingDTO> standings;
}
