package com.secondhand.backend.controller;

import com.secondhand.backend.dto.AdvertisementCreateDto;
import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {
    private final AdvertisementService advertisementService ;
    @PostMapping
    public AdvertisementDto creatAdvertisement(
            @RequestBody AdvertisementCreateDto dto ,
            @RequestParam Long sellerId){
        return advertisementService.createAdvertisement(dto , sellerId) ;
    }
    @GetMapping("/{id}")
    public AdvertisementDto getAdvertisementById(@PathVariable Long id){
        return advertisementService.getAdvertisementById(id) ;
    }
    @GetMapping
    public List<AdvertisementDto> getAllAdvertisements(){
        return advertisementService.getAllActiveAdvertisement() ;
    }
    /*@GetMapping("/search")
    public List<AdvertisementDto> searchAdvertisements(@RequestParam String keywords){
        return advertisementService.searchByTitle(keywords) ;
    }
     */
    @PutMapping("/{id}")
    public AdvertisementDto updateAdvertisement(@PathVariable Long id ,
    @RequestBody AdvertisementDto dto){
        return advertisementService.updateAdvertisement(id , dto) ;
    }
    @DeleteMapping("/{id}")
    public void deleteAdvertisement(@PathVariable Long id){
        advertisementService.deleteAdvertisement(id);
    }
    @PutMapping("/{id}/approve")
    public AdvertisementDto approveAdvertisements(@PathVariable Long id){
        return advertisementService.approveAdvertisement(id) ;
    }
    @PutMapping("/{id}/reject")
    public AdvertisementDto rejectAdvertisement(@PathVariable Long id){
        return advertisementService.rejectAdvertisement(id) ;
    }
    // 👈 وظیفه: ایجاد Endpoint برای سیستم جستجو و فیلتر پیشرفته آگهی‌ها
    @GetMapping("/search")
    public ResponseEntity<List<AdvertisementDto>> filterAds(
            @ModelAttribute com.secondhand.backend.dto.FilterAdvertisementDto filterDto // 👈 اضافه شدن @ModelAttribute
    ) {
        try {
            // صدا زدن لایه سرویس برای اعمال فیلترهای پویا روی دیتابیس
            List<AdvertisementDto> result = advertisementService.searchAndFilter(filterDto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
