package com.secondhand.backend.service;

import com.secondhand.backend.dto.AdvertisementCreateDto;
import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.entity.AdvertisementStatus;

import java.util.List;

public interface AdvertisementService {
    AdvertisementDto createAdvertisement(AdvertisementCreateDto dto , Long sellerId) ;
    AdvertisementDto updateAdvertisement(Long id , AdvertisementDto dto) ;
    void deleteAdvertisement(Long id) ;
    AdvertisementDto getAdvertisementById(Long id) ;

    // 🟢 اضافه شدن متد جدید اورلود شده فعال
    List<AdvertisementDto> getAllActiveAdvertisement(String sortBy, String order);
    List<AdvertisementDto> getAllActiveAdvertisement() ;

    List<AdvertisementDto> searchByTitle(String keyword)  ;

    // 🟢 اضافه شدن متد جدید جستجوی پیشرفته به همراه فیلتر و مرتب‌سازی
    List<AdvertisementDto> searchAdvertisementsAdvanced(String query, Long categoryId, Long cityId, Double minPrice, Double maxPrice, String sortBy, String order);

    AdvertisementDto approveAdvertisement(Long id) ;
    AdvertisementDto rejectAdvertisement(Long id) ;
    AdvertisementDto createAdvertisementByUsername(AdvertisementCreateDto dto,String username);
    List<AdvertisementDto> getAllPendingAdvertisements();
    List<AdvertisementDto> getAllAdvertisements();
    List<AdvertisementDto> getAdvertisementsByStatus(AdvertisementStatus status);
    List<AdvertisementDto> getAdvertisementsBySellerUsername(String username);
    AdvertisementDto markAsSold(Long id);
}