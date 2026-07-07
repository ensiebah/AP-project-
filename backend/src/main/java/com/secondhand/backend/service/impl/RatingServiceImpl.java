package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.RatingDto;

import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.entity.Rating;
import com.secondhand.backend.entity.User;

import com.secondhand.backend.exception.AdvertisementNotFoundException;
import com.secondhand.backend.exception.AlreadyRatedException;
import com.secondhand.backend.exception.UserNotFoundException;

import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.repository.RatingRepository;
import com.secondhand.backend.repository.UserRepository;

import com.secondhand.backend.service.RatingService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final AdvertisementRepository advertisementRepository;

    @Override
    public RatingDto createRating(
            Long buyerId,
            Long advertisementId,
            Integer score,
            String comment
    ) {

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Buyer not found"));

        Advertisement advertisement =
                advertisementRepository.findById(advertisementId)
                        .orElseThrow(() ->
                                new AdvertisementNotFoundException(
                                        "Advertisement not found"));
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Score must be between 1 and 5.");
        }

        if (ratingRepository
                .findByBuyerAndAdvertisement(
                        buyer,
                        advertisement
                )
                .isPresent()) {

            throw new AlreadyRatedException(
                    "You have already rated this advertisement"
            );
        }

        User seller = advertisement.getSeller();

        if (buyer.getId().equals(seller.getId())) {
            throw new IllegalArgumentException(
                    "You cannot rate your own advertisement"
            );
        }

        Rating rating = new Rating();

        rating.setScore(score);
        rating.setComment(comment);

        rating.setBuyer(buyer);
        rating.setSeller(seller);
        rating.setAdvertisement(advertisement);

        Rating savedRating =
                ratingRepository.save(rating);

        return mapToDto(savedRating);
    }

    @Override
    public List<RatingDto> getSellerRatings(
            Long sellerId
    ) {

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Seller not found"));

        return ratingRepository.findBySeller(seller)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private RatingDto mapToDto(
            Rating rating
    ) {

        return RatingDto.builder()
                .id(rating.getId())

                .score(rating.getScore())
                .comment(rating.getComment())

                .buyerId(
                        rating.getBuyer().getId()
                )
                .buyerUsername(
                        rating.getBuyer().getUserName()
                )

                .sellerId(
                        rating.getSeller().getId()
                )
                .sellerUsername(
                        rating.getSeller().getUserName()
                )

                .advertisementId(
                        rating.getAdvertisement().getId()
                )

                .build();
    }

    @Override
    public double getSellerAverageScore(Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException("Seller not found"));

        List<Rating> ratings = ratingRepository.findBySeller(seller);
        if (ratings.isEmpty()) {
            return 0.0;
        }

        return ratings.stream()
                .mapToInt(Rating::getScore)
                .average()
                .orElse(0.0);
    }


}