package com.secondhand.backend.service;

import com.secondhand.backend.dto.AdvertisementCreateDto;
import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.dto.AdvertisementUpdateDto;

import java.util.List;

public interface AdvertisementService {

    AdvertisementDto createAdvertisement(
            Long sellerId,
            AdvertisementCreateDto request
    );

    AdvertisementDto updateAdvertisement(
            Long advertisementId,
            Long sellerId,
            AdvertisementUpdateDto request
    );

    void deleteAdvertisement(
            Long advertisementId,
            Long sellerId
    );

    AdvertisementDto getAdvertisementById(
            Long advertisementId
    );

    List<AdvertisementDto> getAllActiveAdvertisement();

    List<AdvertisementDto> searchByTitle(
            String title
    );

    void approveAdvertisement(
            Long advertisementId
    );

    void rejectAdvertisement(
            Long advertisementId
    );
}