package com.secondhand.backend.service;

import com.secondhand.backend.dto.AdvertisementCreateDto;
import com.secondhand.backend.dto.AdvertisementDto;

import java.util.List;

public interface AdvertisementService {
    AdvertisementDto createAdvertisement(AdvertisementCreateDto dto , Long sellerId) ;
    AdvertisementDto updateAdvertisement(Long id , AdvertisementDto dto) ;
    void deleteAdvertisement(Long id) ;
    AdvertisementDto getAdvertisementById(Long id) ;
    List<AdvertisementDto> getAllActiveAdvertisement() ;
    List<AdvertisementDto> searchByTitle(String keyword)  ;
    AdvertisementDto approveAdvertisement(Long id) ;
    AdvertisementDto rejectAdvertisement(Long id) ;
    AdvertisementDto createAdvertisementByUsername(AdvertisementCreateDto dto,String username);
    List<AdvertisementDto> getAllPendingAdvertisements();
}
