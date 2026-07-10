package com.secondhand.backend.controller;

import com.secondhand.backend.dto.RatingDto;
import com.secondhand.backend.service.RatingService;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;
    private final UserRepository userRepository;

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
}