package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Rating;
import com.secondhand.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.secondhand.backend.entity.Advertisement;
import java.util.Optional;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating,Long> {

    List<Rating> findBySeller(User seller);
    Optional<Rating> findByBuyerAndAdvertisement(
            User buyer,
            Advertisement advertisement
    );
}