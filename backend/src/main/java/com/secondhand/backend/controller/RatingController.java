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


    /**
     * Creates a new rating for an advertisement.
     * The authenticated user is automatically considered the buyer.
     *
     * @param requestDto the rating information
     * @param principal the authenticated user
     * @return the created rating
     */
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


    /**
     * Retrieves all ratings received by a specific seller.
     *
     * @param sellerId the seller ID
     * @return a list of seller ratings
     */
    @GetMapping("/seller/{sellerId}")
    public List<RatingDto> getSellerRatings(@PathVariable Long sellerId) {
        return ratingService.getSellerRatings(sellerId);
    }

    /**
     * Calculates the average rating score of a seller.
     *
     * @param sellerId the seller ID
     * @return the seller's average rating score
     */
    @GetMapping("/seller/{sellerId}/average")
    public double getSellerAverageScore(@PathVariable Long sellerId) {
        return ratingService.getSellerAverageScore(sellerId);
    }

    /**
     * Checks whether the authenticated user is allowed to submit
     * a rating for a specific advertisement.
     * The user cannot rate their own advertisement or submit
     * multiple ratings for the same advertisement.
     *
     * @param adId the advertisement ID
     * @param principal the authenticated user
     * @return a map containing the eligibility result and reason if denied
     */
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