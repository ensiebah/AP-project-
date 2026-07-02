package com.secondhand.backend.service;

import com.secondhand.backend.dto.RatingDto;

import java.util.List;

public interface RatingService {

    RatingDto createRating(
            Long buyerId,
            Long advertisementId,
            Integer score,
            String comment
    );

    List<RatingDto> getSellerRatings(
            Long sellerId
    );
}