package com.secondhand.backend.controller;

import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/advertisements") // هماهنگ با BASE_URL فرانت‌اَند شما
public class SearchController {

    @Autowired
    private AdvertisementRepository advertisementRepository;

    // 🟢 متد جدید جستجو که جایگزین متد قدیمی شما می‌شود
    @GetMapping("/search")
    public ResponseEntity<List<AdvertisementDto>> searchAds(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {

        // ۱. اجرای فیلتر بهینه در سطح دیتابیس با متد ریپازیتوری خودت
        List<Advertisement> results = advertisementRepository.filterAdvertisements(
                query, categoryId, cityId, minPrice, maxPrice
        );

        // ۲. فیلتر کردن آگهی‌های فعال و تبدیل آن‌ها به DTO برای فرانت‌اَند
        List<AdvertisementDto> activeDtos = results.stream()
                .filter(ad -> ad.getStatus() != null && "ACTIVE".equalsIgnoreCase(ad.getStatus().name()))
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(activeDtos);
    }

    // 🟢 متد کمکی برای تبدیل امینت‌های دیتابیس به فرمت DTO فرانت‌اَند
    private AdvertisementDto convertToDto(Advertisement ad) {
        AdvertisementDto dto = new AdvertisementDto();
        dto.setId(ad.getId());
        dto.setTitle(ad.getTitle());
        dto.setDescription(ad.getDescription());
        dto.setPrice(ad.getPrice());
        dto.setStatus(ad.getStatus() != null ? ad.getStatus() : null);

        if (ad.getSeller() != null) {
            dto.setSellerId(ad.getSeller().getId());
            dto.setSellerName(ad.getSeller().getUserName());
        }
        if (ad.getCategory() != null) {
            dto.setCategoryId(ad.getCategory().getId());
            dto.setCategoryName(ad.getCategory().getName());
        }
        if (ad.getCity() != null) {
            dto.setCityId(ad.getCity().getId());
            dto.setCityName(ad.getCity().getName());
        }
        return dto;
    }
}