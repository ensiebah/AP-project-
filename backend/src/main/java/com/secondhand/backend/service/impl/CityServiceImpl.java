package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.CityDto;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation responsible for city management.
 */
@Service

@RequiredArgsConstructor
@Transactional
public class CityServiceImpl implements CityService {
    private final CityRepository cityRepository;

    /**
     * Creates a new city.
     *
     * @param dto city information
     * @return created city
     */
    @Override
    public CityDto creatCity(CityDto dto) {
        cityRepository.findByNameIgnoreCase(dto.getName())
                .ifPresent(city ->
                {throw new RuntimeException("City already exists") ;});
        City city = new City() ;
        city.setName(dto.getName());
        City saved = cityRepository.save(city) ;
        return mapToDto(saved) ;
    }

    /**
     * Retrieves all cities.
     *
     * @return list of cities
     */
    @Override
    public List<CityDto> getAllCities() {
        return cityRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Retrieves a city by its identifier.
     *
     * @param id city identifier
     * @return city information
     */
    @Override
    public CityDto getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(()->new RuntimeException("City not found")) ;
        return mapToDto(city) ;
    }

    /**
     * Deletes a city.
     *
     * @param id city identifier
     */
    @Override
    public void deleteCity(Long id) {

        cityRepository.deleteById(id);
    }
    private CityDto mapToDto(City city){

        return CityDto.builder()
                .id(city.getId())
                .name(city.getName())
                .build();
    }
}
