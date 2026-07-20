package com.secondhand.backend.controller;

import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.entity.AdvertisementStatus;
import com.secondhand.backend.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/advertisements") // هماهنگ با BASE_URL فرانت‌اَند
public class SearchController {

    @Autowired
    private AdvertisementRepository advertisementRepository;

    /**
     * Searches active advertisements using optional filters such as
     * keyword, category, city, price range, and sorting options.
     *
     * @param query the search keyword
     * @param categoryId the category ID used for filtering
     * @param cityId the city ID used for filtering
     * @param minPrice the minimum advertisement price
     * @param maxPrice the maximum advertisement price
     * @param sortBy the field used for sorting
     * @param order the sorting order (asc or desc)
     * @return a list of matching advertisements
     */
    // 🟢 متد بهینه و نهایی جستجو همراه با پشتیبانی از فیلترها و مرتب‌سازی دیتابیس
    @GetMapping("/search")
    public ResponseEntity<List<AdvertisementDto>> searchAds(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String order
    ) {

        // اجرای فیلتر و مرتب‌سازی پیشرفته و کاملاً پویا در سطح دیتابیس
        List<Advertisement> results = advertisementRepository.filterAdvertisementsAdvanced(
                AdvertisementStatus.ACTIVE,
                query,
                categoryId,
                cityId,
                minPrice,
                maxPrice,
                sortBy,
                order
        );

        // تبدیل نتیجه به DTO برای فرستادن به فرانت‌اَند
        List<AdvertisementDto> activeDtos = results.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(activeDtos);
    }



    // متد کمکی برای تبدیل Entity به DTO
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