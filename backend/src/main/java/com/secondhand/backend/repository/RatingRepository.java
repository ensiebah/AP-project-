package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Rating;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    // 🆕 این خط را اضافه کنید تا سیستم بررسی کند آیا خریدار قبلاً به این آگهی رای داده یا خیر
    boolean existsByBuyerAndAdvertisement(User buyer, Advertisement advertisement);

    List<Rating> findBySeller(User seller);
}