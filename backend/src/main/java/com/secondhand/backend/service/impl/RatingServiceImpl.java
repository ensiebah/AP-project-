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
    public RatingDto createRating(Long buyerId, Long advertisementId, Integer score, String comment) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new UserNotFoundException("Buyer not found"));

        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException("Advertisement not found"));

        // بند ۷: بررسی ولید بودن بازه امتیاز
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Score must be between 1 and 5.");
        }

        // بند ۸: بررسی عدم وجود امتیاز تکراری برای این آگهی
        // 👈 اصلاح خط خطا: به جای .isPresent() مستقیماً از متد exists استفاده کن
        if (ratingRepository.existsByBuyerAndAdvertisement(buyer, advertisement)) {
            throw new AlreadyRatedException("You have already rated the seller for this advertisement");
        }

        User seller = advertisement.getSeller();

        // بند ۶: بررسی اینکه کاربر به خودش امتیاز ندهد
        if (buyer.getId().equals(seller.getId())) {
            throw new IllegalArgumentException("You cannot rate yourself.");
        }

        // بند ۹: ذخیره سازی امتیاز در سیستم
        Rating rating = new Rating();
        rating.setScore(score);
        rating.setComment(comment);
        rating.setBuyer(buyer);
        rating.setSeller(seller);
        rating.setAdvertisement(advertisement);

        Rating savedRating = ratingRepository.save(rating);
        return mapToDto(savedRating);
    }

    @Override
    public List<RatingDto> getSellerRatings(Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException("Seller not found"));

        return ratingRepository.findBySeller(seller).stream()
                .map(this::mapToDto)
                .collect(java.util.stream.Collectors.toList()); // 👈 اصلاح به حالت Collect استاندارد
    }

    @Override
    public double getSellerAverageScore(Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException("Seller not found"));
        List<Rating> ratings = ratingRepository.findBySeller(seller);
        if (ratings.isEmpty()) {
            return 0.0; // بند ۱۰: مقدار پیش‌فرض در صورت عدم وجود امتیاز
        }
        double sum = 0;
        for (Rating r : ratings) {
            sum += r.getScore();
        }
        return sum / ratings.size();
    }

    private RatingDto mapToDto(Rating rating) {
        return RatingDto.builder()
                .id(rating.getId())
                .score(rating.getScore())
                .comment(rating.getComment())
                .buyerId(rating.getBuyer().getId())
                .buyerUsername(rating.getBuyer().getUserName())
                .sellerId(rating.getSeller().getId())
                .sellerUsername(rating.getSeller().getUserName())
                .advertisementId(rating.getAdvertisement().getId())
                .build();
    }
}