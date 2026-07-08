package com.secondhand.backend.controller;

import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.entity.AdvertisementImage;
import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.service.AdvertisementImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController // 👈 نشان می‌دهد این کلاس مسئول پاسخگویی به درخواست‌های وب (API) است
@RequestMapping("/api/images") // 👈 مسیر پایه برای تمام متدهای این کلاس
@RequiredArgsConstructor // 👈 برای تزریق خودکارِ سرویس و ریپازیتوری توسط لومبوک
public class AdvertisementImageController {

    private final AdvertisementImageService imageService; // 👈 تزریق سرویس برای انجام عملیات آپلود
    private final AdvertisementRepository advertisementRepository; // 👈 برای پیدا کردن آگهی در دیتابیس

    // 👈 تعریف متد POST برای دریافت فایل و آیدی آگهی
    @PostMapping("/upload/{advertisementId}")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long advertisementId, // 👈 گرفتن آیدی آگهی از آدرس (URL)
            @RequestParam("file") MultipartFile file // 👈 گرفتن فایلِ عکس از درخواست کلاینت
    ) {
        try {
            // ۱. ابتدا آگهی را از دیتابیس پیدا می‌کنیم تا مطمئن شویم وجود دارد
            Advertisement advertisement = advertisementRepository.findById(advertisementId).
                    orElseThrow(() -> new RuntimeException("آگهی یافت نشد!"));

            // ۲. فایل و آگهی را به سرویس می‌دهیم تا عملیات ذخیره‌سازی انجام شود
            AdvertisementImage savedImage = imageService.uploadImage(file, advertisement);

            // ۳. پاسخ موفقیت‌آمیز برمی‌گردانیم
            return ResponseEntity.ok("عکس با موفقیت آپلود شد: " + savedImage.getImagePath());

        } catch (Exception e) {
            // ۴. اگر خطایی رخ داد (مثل فایل نبودن یا پر بودن دیسک)، خطا را برمی‌گردانیم
            return ResponseEntity.badRequest().body("خطا در آپلود عکس: " + e.getMessage());
        }
    }
}