package com.secondhand.backend.service;

import com.secondhand.backend.dto.CityDto;

import java.util.List;

public interface CityService {
    CityDto creatCity(CityDto dto) ;
    List<CityDto> getAllCities() ;
    CityDto getCityById(Long id) ;
    void deleteCity(Long id);

}
