package com.secondhand.backend.controller;

import com.secondhand.backend.dto.AdminDashboardSummaryDto;
import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.dto.AdvertisementRejectRequest;
import com.secondhand.backend.dto.CategoryDto;
import com.secondhand.backend.entity.AdvertisementStatus;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.AdvertisementService;
import com.secondhand.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** APIs consumed only by the administrator dashboard. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdvertisementService advertisementService;
    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    /** Summary values for the overview cards. */
    @GetMapping("/dashboard/summary")
    public AdminDashboardSummaryDto getSummary() {
        long blockedUsers = userRepository.findAll().stream()
                .filter(User::isBlocked)
                .count();

        return AdminDashboardSummaryDto.builder()
                .totalAdvertisements(advertisementRepository.count())
                .pendingAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.PENDING))
                .activeAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.ACTIVE))
                .rejectedAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.REJECTED))
                .soldAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.SOLD))
                .deletedAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.DELETED))
                .totalUsers(userRepository.count())
                .blockedUsers(blockedUsers)
                .totalCities(cityRepository.count())
                .totalCategories(categoryRepository.count())
                .rootCategories(categoryRepository.countByParentIsNull())
                .build();
    }

    /**
     * Filter every lifecycle state from one endpoint. Valid values are ALL,
     * PENDING, ACTIVE, REJECTED, SOLD and DELETED.
     */
    @GetMapping("/advertisements")
    public ResponseEntity<List<AdvertisementDto>> getAdvertisements(
            @RequestParam(defaultValue = "ALL") String status
    ) {
        if ("ALL".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(advertisementService.getAllAdvertisements());
        }
        try {
            AdvertisementStatus requestedStatus = AdvertisementStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(advertisementService.getAdvertisementsByStatus(requestedStatus));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** Flat list used by the client to build a root/child category tree. */
    @GetMapping("/categories")
    public List<CategoryDto> getAllCategories() {
        return categoryService.getAllCategories();
    }

    /** Creates a root category or a child of dto.parentId. */
    @PostMapping("/categories")
    public CategoryDto createCategory(@RequestBody CategoryDto dto) {
        if (dto.getParentId() == null) {
            return categoryService.createCategory(dto.getName());
        }
        return categoryService.createSubcategory(dto.getName(), dto.getParentId());
    }

    @PutMapping("/categories/{id}")
    public CategoryDto renameCategory(@PathVariable Long id, @RequestBody CategoryDto dto) {
        return categoryService.updateCategoryName(id, dto.getName());
    }

    /**
     * Safe delete: the service rejects categories which still have child
     * categories or advertisements, avoiding destructive cascades.
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/advertisements/pending")
    public ResponseEntity<List<AdvertisementDto>> getPendingAdvertisements() {
        return ResponseEntity.ok(advertisementService.getAllPendingAdvertisements());
    }

    @PutMapping("/advertisements/{id}/approve")
    public ResponseEntity<AdvertisementDto> approveAdvertisement(@PathVariable Long id) {
        return ResponseEntity.ok(advertisementService.approveAdvertisement(id));
    }

    @PutMapping("/advertisements/{id}/reject")
    public ResponseEntity<AdvertisementDto> rejectAdvertisement(
            @PathVariable Long id,
            @Valid @RequestBody AdvertisementRejectRequest request
    ) {
        return ResponseEntity.ok(advertisementService.rejectAdvertisement(id, request.getReason()));
    }
}
