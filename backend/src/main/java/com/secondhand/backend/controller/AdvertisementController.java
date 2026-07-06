package com.secondhand.backend.controller;

import com.secondhand.backend.dto.AdvertisementCreateDto;
import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advertisements") // افزودن /api برای هماهنگی با فرانت‌آند
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    /**
     * 🟢 وظیفه: ثبت آگهی جدید در سیستم
     * 🎯 پوشش خواسته داک: سناریوی ثبت آگهی توسط کاربر لاگین‌شده.
     * 🔍 چطور کار می‌کند؟ به جای @RequestParam، از شیء Principal استفاده شده است.
     * اسپرینگ‌بوت به صورت خودکار نام کاربری را از روی توکن JWT معتبرِ هدر استخراج کرده و به این متد تزریق می‌کند.
     */
    @PostMapping("/create")
    public AdvertisementDto createAdvertisement(@RequestBody AdvertisementCreateDto dto, Principal principal) {
        // نام کاربری استخراج شده از توکن را به سرویس می‌فرستیم
        String username = principal.getName();
        return advertisementService.createAdvertisementByUsername(dto, username);
    }

    @GetMapping("/{id}")
    public AdvertisementDto getAdvertisementById(@PathVariable Long id) {
        return advertisementService.getAdvertisementById(id);
    }

    /**
     * 🔵 وظیفه: بازگرداندن تمام آگهی‌های تایید شده و فعال در صفحه اصلی مارکت
     */
    @GetMapping("/active")
    public List<AdvertisementDto> getAllActiveAdvertisements() {
        return advertisementService.getAllActiveAdvertisement();
    }

    /**
     * 🟡 وظیفه: جست‌وجوی آگهی‌ها بر اساس متن ارسالی از فرانت‌آند
     * 🔍 چطور کار می‌کند؟ فرانت‌آند متن را به صورت {"query": "text"} با متد POST می‌فرستد.
     * ما اینجا آن را به صورت یک Map دریافت کرده و فیلد query را برمی‌داریم تا با فرانت مچ باشد.
     */
    @PostMapping("/search")
    public List<AdvertisementDto> searchAdvertisements(@RequestBody Map<String, String> searchRequest) {
        String keywords = searchRequest.getOrDefault("query", "");
        return advertisementService.searchByTitle(keywords);
    }

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
}