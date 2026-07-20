package com.secondhand.backend.controller;

import com.secondhand.backend.dto.CityDto;
import com.secondhand.backend.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cities")
@RequiredArgsConstructor
public class CityController {
    private final CityService cityService ;
    /**
     * Creates a new city.
     *
     * @param dto the city information
     * @return the created city
     */
    @PostMapping
    public CityDto createCity(@RequestBody CityDto dto){
        return cityService.creatCity(dto) ;
    }

    /**
     * Retrieves all available cities.
     *
     * @return a list of cities
     */
    @GetMapping
    public List<CityDto> getAllCities(){
        return cityService.getAllCities() ;
    }

    /**
     * Retrieves a city by its unique identifier.
     *
     * @param id the city ID
     * @return the requested city
     */
    @GetMapping("/{id}")
    public CityDto getCityById(@PathVariable Long id){
        return cityService.getCityById(id) ;
    }

    /**
     * Deletes a city by its unique identifier.
     *
     * @param id the city ID
     */
    @DeleteMapping("/{id}")
    public void deleteCity(@PathVariable Long id){
        cityService.deleteCity(id);
    }
}
