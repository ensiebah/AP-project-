package com.secondhand.backend.controller;

import com.secondhand.backend.dto.RatingDto;
import com.secondhand.backend.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    /**
     * 📥 ثبت امتیاز جدید برای فروشنده توسط خریدار
     */
    @PostMapping
    public RatingDto createRating(@RequestBody RatingDto requestDto) {
        return ratingService.createRating(
                requestDto.getBuyerId(),
                requestDto.getAdvertisementId(),
                requestDto.getScore(),
                requestDto.getComment()
        );
    }

    /**
     * 📤 دریافت تمام امتیازها و نظرات یک فروشنده بر اساس شناسه او
     */
    @GetMapping("/seller/{sellerId}")
    public List<RatingDto> getSellerRatings(@PathVariable Long sellerId) {
        return ratingService.getSellerRatings(sellerId);
    }

    /**
     * ⭐ دریافت میانگین امتیازهای یک فروشنده
     */
    @GetMapping("/seller/{sellerId}/average")
    public double getSellerAverageScore(@PathVariable Long sellerId) {
        return ratingService.getSellerAverageScore(sellerId);
    }
}