package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.entity.AdvertisementImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdvertisementImageRepository extends JpaRepository<AdvertisementImage, Long> {

    List<AdvertisementImage> findByAdvertisementOrderByIdAsc(Advertisement advertisement);

    Optional<AdvertisementImage> findByAdvertisementAndImagePath(
            Advertisement advertisement,
            String imagePath
    );
}
