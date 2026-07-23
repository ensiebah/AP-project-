package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.entity.AdvertisementStatus;
import com.secondhand.backend.entity.Category;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    List<Advertisement> findByStatus(AdvertisementStatus status, Sort sort);

    List<Advertisement> findByStatus(AdvertisementStatus status);

    long countByStatus(AdvertisementStatus status);

    List<Advertisement> findBySeller(User seller);

    List<Advertisement> findByCategory(Category category);

    List<Advertisement> findByCity(City city);

    List<Advertisement> findByTitleContainingIgnoreCase(String title);

    Long id(Long id);

    List<Advertisement> findBySellerAndStatusNotOrderByIdDesc(User seller, AdvertisementStatus status);

    /**
     * If categoryId is a root category, a.category.parent.id matches all of
     * its subcategory ads. If it is a leaf category, a.category.id matches
     * only that subcategory's ads.
     */
    @Query("SELECT a FROM Advertisement a " +
            "LEFT JOIN Rating r ON r.seller.id = a.seller.id " +
            "WHERE a.status = :status AND " +
            "(:query IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:categoryId IS NULL OR a.category.id = :categoryId OR a.category.parent.id = :categoryId) AND " +
            "(:cityId IS NULL OR a.city.id = :cityId) AND " +
            "(:minPrice IS NULL OR a.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR a.price <= :maxPrice) " +
            "GROUP BY a.id " +
            "ORDER BY " +
            "  CASE WHEN :sortBy = 'price' AND :order = 'asc' THEN a.price END ASC, " +
            "  CASE WHEN :sortBy = 'price' AND :order = 'desc' THEN a.price END DESC, " +
            "  CASE WHEN :sortBy = 'date' AND :order = 'asc' THEN a.id END ASC, " +
            "  CASE WHEN :sortBy = 'date' AND :order = 'desc' THEN a.id END DESC, " +
            "  CASE WHEN :sortBy = 'rating' AND :order = 'asc' THEN COALESCE(AVG(r.score), 0.0) END ASC, " +
            "  CASE WHEN :sortBy = 'rating' AND :order = 'desc' THEN COALESCE(AVG(r.score), 0.0) END DESC, " +
            "  a.id DESC")
    List<Advertisement> filterAdvertisementsAdvanced(
            @Param("status") AdvertisementStatus status,
            @Param("query") String query,
            @Param("categoryId") Long categoryId,
            @Param("cityId") Long cityId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("sortBy") String sortBy,
            @Param("order") String order
    );
}
