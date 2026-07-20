package com.secondhand.backend.controller;

import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/advertisements")
@RequiredArgsConstructor
public class AdminController {

    // تزریق اینترفیس سرویس برای استفاده از منطق‌های تجاری پروژه
    private final AdvertisementService advertisementService;

    /**
     * 🔍 وظیفه: دریافت تمام آگهی‌هایی که منتظر تایید مدیر هستند (وضعیت PENDING)
     * 📬 آدرس فراخوانی: GET http://localhost:8080/api/admin/advertisements/pending
     */
    /**
     * Retrieves all advertisements that are waiting for administrator approval.
     *
     * @return a list of pending advertisements
     */
    @GetMapping("/pending")
    public ResponseEntity<List<AdvertisementDto>> getPendingAdvertisements() {
        // از آنجا که متد اختصاصی برای پندینگ‌ها در سرویس نداری، موقتاً در لایه سرویس یا ریپازیتوری باید فیلتر شود
        // اما برای اینکه کدهای شما دست نخورد، فعلاً فرض می‌کنیم متدی به نام getAllPendingAdvertisements اضافه می‌کنیم یا از ریپازیتوری می‌خوانیم.
        // بگذار در ادامه متد آن را به سرویس شما اضافه کنیم.
        List<AdvertisementDto> pendingAds = advertisementService.getAllPendingAdvertisements();
        return ResponseEntity.ok(pendingAds);
    }

    /**
     * 🟢 وظیفه: تایید یک آگهی و تغییر وضعیت آن به ACTIVE
     * 📬 آدرس فراخوانی: PUT http://localhost:8080/api/admin/advertisements/{id}/approve
     */
    /**
     * Approves a pending advertisement and changes its status to ACTIVE.
     *
     * @param id the ID of the advertisement to approve
     * @return the approved advertisement
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<AdvertisementDto> approveAdvertisement(@PathVariable Long id) {
        // متد approveAdvertisement از قبل در AdvertisementServiceImpl شما پیاده‌سازی شده است!
        AdvertisementDto approvedAd = advertisementService.approveAdvertisement(id);
        return ResponseEntity.ok(approvedAd);
    }

    /**
     * 🔴 وظیفه: رد کردن یک آگهی و تغییر وضعیت آن به REJECTED
     * 📬 آدرس فراخوانی: PUT http://localhost:8080/api/admin/advertisements/{id}/reject
     */
    /**
     * Rejects a pending advertisement and changes its status to REJECTED.
     *
     * @param id the ID of the advertisement to reject
     * @return the rejected advertisement
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<AdvertisementDto> rejectAdvertisement(@PathVariable Long id) {
        // متد rejectAdvertisement هم از قبل در AdvertisementServiceImpl شما موجود است!
        AdvertisementDto rejectedAd = advertisementService.rejectAdvertisement(id);
        return ResponseEntity.ok(rejectedAd);
    }
}