/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.teamscontroller;

import com.pes.copa.teams.entity.Country;
import com.pes.copa.teams.service.CountryService;
import com.pes.copa.teams.service.TeamsService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author sangr
 */
@RestController
public class CountryController {
    private CountryService countryService;
    
    @GetMapping("country/continent/{continent}")
    public List<Country> getCountriesByContinent(@PathVariable String continent){
        return countryService.getCountriesByContinent(continent);
    }
    
    @GetMapping("country/mundialist")
    public List<Country> getCountriesByIsMundialist(){
        return countryService.getCountriesByIsMundialist();
    }
    
    
}
