package com.secondhand.backend.controller;

import com.secondhand.backend.dto.RatingDto;
import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.repository.RatingRepository;
import com.secondhand.backend.service.RatingService;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;
    private final UserRepository userRepository;
    private final AdvertisementRepository advertisementRepository ;
    private final RatingRepository ratingRepository ;

    @PostMapping
    public RatingDto createRating(@RequestBody RatingDto requestDto, Principal principal) {
        // استخراج خریدار از روی توکن لاگین شده
        String username = principal.getName();
        User currentUser = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        return ratingService.createRating(
                currentUser.getId(),
                requestDto.getAdvertisementId(),
                requestDto.getScore(),
                requestDto.getComment()
        );
    }

    @GetMapping("/seller/{sellerId}")
    public List<RatingDto> getSellerRatings(@PathVariable Long sellerId) {
        return ratingService.getSellerRatings(sellerId);
    }

    @GetMapping("/seller/{sellerId}/average")
    public double getSellerAverageScore(@PathVariable Long sellerId) {
        return ratingService.getSellerAverageScore(sellerId);
    }
    @GetMapping("/check-eligibility/{adId}")
    public Map<String, Object> checkEligibility(@PathVariable Long adId, Principal principal) {
        String username = principal.getName();
        User currentUser = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        // ۱. بررسی اینکه کاربر خودش فروشنده نباشد
        if (ad.getSeller().getId().equals(currentUser.getId())) {
            return Map.of("allowed", false, "reason", "You cannot rate your own advertisement.");
        }

        // ۲. بررسی اینکه خریدار قبلاً برای این آگهی رای ثبت نکرده باشد
        boolean alreadyRated = ratingRepository.existsByBuyerAndAdvertisement(currentUser, ad);
        if (alreadyRated) {
            return Map.of("allowed", false, "reason", "You have already submitted a rating for this advertisement.");
        }

        return Map.of("allowed", true);
    }
}