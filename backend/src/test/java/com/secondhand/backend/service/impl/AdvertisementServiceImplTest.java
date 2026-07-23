package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.AdvertisementCreateDto;
import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.entity.*;
import com.secondhand.backend.exception.UserNotFoundException;
import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceImplTest {

    @Mock
    private AdvertisementRepository advertisementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private AdvertisementServiceImpl advertisementService;

    @Test
    void createAdvertisementByUsername_success() {

        AdvertisementCreateDto dto = new AdvertisementCreateDto();
        dto.setTitle("Laptop");
        dto.setDescription("Gaming Laptop");
        dto.setPrice(2500.0);
        dto.setCategoryId(1L);
        dto.setCityId(1L);

        User seller = new User();
        seller.setId(10L);
        seller.setUserName("ensie");

        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        City city = new City();
        city.setId(1L);
        city.setName("Tehran");

        Advertisement savedAd = new Advertisement();
        savedAd.setId(100L);
        savedAd.setTitle(dto.getTitle());
        savedAd.setDescription(dto.getDescription());
        savedAd.setPrice(dto.getPrice());
        savedAd.setSeller(seller);
        savedAd.setCategory(category);
        savedAd.setCity(city);
        savedAd.setStatus(AdvertisementStatus.PENDING);

        when(userRepository.findByUserName("ensie"))
                .thenReturn(Optional.of(seller));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(cityRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(advertisementRepository.save(any(Advertisement.class)))
                .thenReturn(savedAd);

        AdvertisementDto result =
                advertisementService.createAdvertisementByUsername(dto, "ensie");

        assertNotNull(result);
        assertEquals("Laptop", result.getTitle());
        assertEquals("Gaming Laptop", result.getDescription());
        assertEquals(2500.0, result.getPrice());
        assertEquals("ensie", result.getSellerName());
        assertEquals("Electronics", result.getCategoryName());
        assertEquals("Tehran", result.getCityName());
        assertEquals(AdvertisementStatus.PENDING, result.getStatus());

        verify(userRepository).findByUserName("ensie");
        verify(categoryRepository).findById(1L);
        verify(cityRepository).findById(1L);
        verify(advertisementRepository).save(any(Advertisement.class));
    }

    @Test
    void createAdvertisementByUsername_userNotFound() {

        AdvertisementCreateDto dto = new AdvertisementCreateDto();

        when(userRepository.findByUserName("ensie"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> advertisementService.createAdvertisementByUsername(dto, "ensie")
        );

        verify(advertisementRepository, never()).save(any());
    }
}