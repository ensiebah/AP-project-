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

    @PostMapping
    public RatingDto createRating(
            @RequestParam Long buyerId,
            @RequestParam Long advertisementId,
            @RequestParam Integer score,
            @RequestParam String comment
    ) {
        return ratingService.createRating(
                buyerId,
                advertisementId,
                score,
                comment
        );
    }

    @GetMapping("/seller/{sellerId}")
    public List<RatingDto> getSellerRatings(
            @PathVariable Long sellerId
    ) {
        return ratingService.getSellerRatings(sellerId);
    }
}