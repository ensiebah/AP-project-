package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.CityDto;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.repository.CityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceImplTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CityServiceImpl cityService;

    @Test
    void createCity_ShouldCreateSuccessfully() {

        CityDto dto = CityDto.builder()
                .name("Mashhad")
                .build();

        City city = new City();
        city.setId(1L);
        city.setName("Mashhad");

        when(cityRepository.findByNameIgnoreCase("Mashhad"))
                .thenReturn(Optional.empty());

        when(cityRepository.save(any(City.class)))
                .thenReturn(city);

        CityDto result = cityService.creatCity(dto);

        assertNotNull(result);
        assertEquals("Mashhad", result.getName());
        assertEquals(1L, result.getId());

        verify(cityRepository).save(any(City.class));
    }

    @Test
    void createCity_WhenAlreadyExists_ShouldThrowException() {

        CityDto dto = CityDto.builder()
                .name("Mashhad")
                .build();

        City city = new City();
        city.setId(1L);
        city.setName("Mashhad");

        when(cityRepository.findByNameIgnoreCase("Mashhad"))
                .thenReturn(Optional.of(city));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cityService.creatCity(dto)
        );

        assertEquals("City already exists", exception.getMessage());

        verify(cityRepository, never()).save(any());
    }

    @Test
    void getCityById_ShouldReturnCity() {

        City city = new City();
        city.setId(1L);
        city.setName("Tehran");

        when(cityRepository.findById(1L))
                .thenReturn(Optional.of(city));

        CityDto dto = cityService.getCityById(1L);

        assertEquals(1L, dto.getId());
        assertEquals("Tehran", dto.getName());
    }

    @Test
    void getCityById_WhenNotFound_ShouldThrowException() {

        when(cityRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cityService.getCityById(1L)
        );

        assertEquals("City not found", exception.getMessage());
    }

    @Test
    void getAllCities_ShouldReturnAllCities() {

        City c1 = new City();
        c1.setId(1L);
        c1.setName("Mashhad");

        City c2 = new City();
        c2.setId(2L);
        c2.setName("Tehran");

        when(cityRepository.findAll())
                .thenReturn(List.of(c1, c2));

        List<CityDto> result = cityService.getAllCities();

        assertEquals(2, result.size());
        assertEquals("Mashhad", result.get(0).getName());
        assertEquals("Tehran", result.get(1).getName());
    }

    @Test
    void deleteCity_ShouldDeleteSuccessfully() {

        doNothing().when(cityRepository).deleteById(1L);

        cityService.deleteCity(1L);

        verify(cityRepository).deleteById(1L);
    }

}