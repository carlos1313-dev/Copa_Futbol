/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.teamscontroller;

import com.pes.copa.teams.entity.Teams;
import com.pes.copa.teams.service.TeamsService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author sangr
 */
@RestController
public class TeamsController {
    @Autowired
    private TeamsService teamsService;
    
    @GetMapping("/teams/country/{country}")
    public List<Teams> getTeamsByCountry(@PathVariable String country){
        return teamsService.getTeamsByCountry(country);
    }
    
    @GetMapping("/teams/continent/{continent}")
    public List<Teams> getTeamsByContinent(@PathVariable String continent){
        return teamsService.getTeamsByContinent(continent);
    }
    
    @GetMapping("/teams/all")
    public List<Teams> getAllTeams(){
        return teamsService.getAllTeams();
    }
}
