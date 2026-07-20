package com.secondhand.backend.controller;

import com.secondhand.backend.dto.AdvertisementCreateDto;
import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService advertisementService;
    /**
     * Creates a new advertisement for the currently authenticated user.
     *
     * @param dto the advertisement information
     * @param principal the authenticated user
     * @return the created advertisement
     */
    @PostMapping("/create")
    public AdvertisementDto createAdvertisement(@RequestBody AdvertisementCreateDto dto, Principal principal) {
        String username = principal.getName();
        return advertisementService.createAdvertisementByUsername(dto, username);
    }

    /**
     * Retrieves an advertisement by its unique identifier.
     *
     * @param id the advertisement ID
     * @return the requested advertisement
     */
    @GetMapping("/{id}")
    public AdvertisementDto getAdvertisementById(@PathVariable Long id) {
        return advertisementService.getAdvertisementById(id);
    }

    /**
     * Retrieves all active advertisements with optional sorting.
     *
     * @param sortBy the field used for sorting
     * @param order the sorting order (asc or desc)
     * @return a list of active advertisements
     */
    // 🟢 اصلاح متد دریافت آگهی‌های فعال همراه با پارامترهای مرتب‌سازی
    @GetMapping("/active")
    public List<AdvertisementDto> getAllActiveAdvertisements(
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {
        return advertisementService.getAllActiveAdvertisement(sortBy, order);
    }

    /**
     * Retrieves all advertisements with PENDING status.
     *
     * @return a list of pending advertisements
     */
    @GetMapping("/pending")
    public List<AdvertisementDto> getAllPendingAdvertisements() {
        return advertisementService.getAllPendingAdvertisements();
    }

    /**
     * Retrieves all advertisements created by the authenticated user.
     *
     * @param principal the authenticated user
     * @return a list of the user's advertisements
     */
    @GetMapping("/my-ads")
    public List<AdvertisementDto> getMyAdvertisements(Principal principal) {
        String username = principal.getName();
        return advertisementService.getAdvertisementsBySellerUsername(username);
    }

//    // 🟢 اصلاح متد سرچ: تبدیل از POST به GET برای هماهنگی با فرانت‌اند و اضافه کردن پارامترهای پیشرفته
//    @GetMapping("/search")
//    public List<AdvertisementDto> searchAdvertisements(
//            @RequestParam(required = false) String query,
//            @RequestParam(required = false) Long categoryId,
//            @RequestParam(required = false) Long cityId,
//            @RequestParam(required = false) Double minPrice,
//            @RequestParam(required = false) Double maxPrice,
//            @RequestParam(defaultValue = "date") String sortBy,
//            @RequestParam(defaultValue = "desc") String order) {
//        return advertisementService.searchAdvertisementsAdvanced(query, categoryId, cityId, minPrice, maxPrice, sortBy, order);
//    }

    /**
     * Updates an existing advertisement.
     *
     * @param id the advertisement ID
     * @param dto the updated advertisement information
     * @return the updated advertisement
     */
    @PutMapping("/{id}")
    public AdvertisementDto updateAdvertisement(@PathVariable Long id, @RequestBody AdvertisementDto dto) {
        return advertisementService.updateAdvertisement(id, dto);
    }



    @DeleteMapping("/{id}")
    public void deleteAdvertisement(@PathVariable Long id) {
        advertisementService.deleteAdvertisement(id);
    }

    @PutMapping("/{id}/approve")
    public AdvertisementDto approveAdvertisements(@PathVariable Long id) {
        return advertisementService.approveAdvertisement(id);
    }

    @PutMapping("/{id}/reject")
    public AdvertisementDto rejectAdvertisement(@PathVariable Long id) {
        return advertisementService.rejectAdvertisement(id);
    }

    @PutMapping("/{id}/sold")
    public AdvertisementDto markAsSold(@PathVariable Long id) {
        return advertisementService.markAsSold(id);
    }

}