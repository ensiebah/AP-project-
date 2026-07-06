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
    @PostMapping
    public CityDto createCity(@RequestBody CityDto dto){
        return cityService.creatCity(dto) ;
    }
    @GetMapping
    public List<CityDto> getAllCities(){
        return cityService.getAllCities() ;
    }
    @GetMapping("/{id}")
    public CityDto getCityById(@PathVariable Long id){
        return cityService.getCityById(id) ;
    }
    @DeleteMapping("/{id}")
    public void deleteCity(@PathVariable Long id){
        cityService.deleteCity(id);
    }
}
