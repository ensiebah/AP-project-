package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.entity.AdvertisementStatus;
import com.secondhand.backend.entity.Category;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    List<Advertisement> findByStatus(AdvertisementStatus status);

    List<Advertisement> findBySeller(User seller);

    List<Advertisement> findByCategory(Category category);

    List<Advertisement> findByCity(City city);

    List<Advertisement> findByTitleContainingIgnoreCase(String title);

    Long id(Long id);

    @Query("SELECT a FROM Advertisement a WHERE " +
            "(:query IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:categoryId IS NULL OR a.category.id = :categoryId) AND " + // 👈 مقایسه با آیدیِ دسته‌بندی
            "(:cityId IS NULL OR a.city.id = :cityId) AND " +             // 👈 مقایسه با آیدیِ شهر
            "(:minPrice IS NULL OR a.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR a.price <= :maxPrice)")
    List<Advertisement> filterAdvertisements(
            @Param("query") String query,
            @Param("categoryId") Long categoryId, // 👈 جنس ورودی Long شد
            @Param("cityId") Long cityId,         // 👈 جنس ورودی Long شد
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );
}

