package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.AdvertisementCreateDto;
import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.dto.AdvertisementImageDto;
import com.secondhand.backend.entity.*;
import com.secondhand.backend.exception.CategoryNotFoundException;
import com.secondhand.backend.exception.UserNotFoundException;
import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.AdvertisementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation responsible for managing advertisements.
 * Provides operations for creating, updating, deleting, searching,
 * approving, rejecting, and retrieving advertisements.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;

    /**
     * Creates a new advertisement for the specified user.
     *
     * @param dto advertisement information
     * @param username seller username
     * @return created advertisement
     */
    @Override
    public AdvertisementDto createAdvertisementByUsername(AdvertisementCreateDto dto, String username) {
        User seller = userRepository.findByUserName(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        // An advertisement must be stored in a selectable leaf category, not a broad parent.
        Category category = getLeafCategory(dto.getCategoryId());

        City city = cityRepository.findById(dto.getCityId())
                .orElseGet(() -> {
                    City newCity = new City();
                    newCity.setId(dto.getCityId());
                    newCity.setName("City " + dto.getCityId());
                    return cityRepository.save(newCity);
                });

        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(dto.getTitle());
        advertisement.setDescription(dto.getDescription());
        advertisement.setPrice(dto.getPrice());
        advertisement.setSeller(seller);
        advertisement.setCategory(category);
        advertisement.setCity(city);
        advertisement.setStatus(AdvertisementStatus.PENDING);

        Advertisement saved = advertisementRepository.save(advertisement);
        return mapToDto(saved);
    }


    /**
     * Creates a new advertisement using the seller identifier.
     *
     * @param dto advertisement information
     * @param sellerId seller identifier
     * @return created advertisement
     */
    @Override
    public AdvertisementDto createAdvertisement(AdvertisementCreateDto dto, Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException("seller not found"));
        return createAdvertisementByUsername(dto, seller.getUserName());
    }


    /**
     * Updates an existing advertisement.
     *
     * @param id advertisement identifier
     * @param dto updated advertisement information
     * @return updated advertisement
     */
    @Override
    public AdvertisementDto updateAdvertisement(Long id, AdvertisementDto dto) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));

        // Editing follows the same rule as creation: only a subcategory can be assigned.
        Category category = getLeafCategory(dto.getCategoryId());

        City city = cityRepository.findById(dto.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found"));

        advertisement.setTitle(dto.getTitle());
        advertisement.setDescription(dto.getDescription());
        advertisement.setPrice(dto.getPrice());
        advertisement.setCategory(category);
        advertisement.setCity(city);

        Advertisement updated = advertisementRepository.save(advertisement);
        return mapToDto(updated);
    }

    /**
     * Marks an advertisement as deleted.
     *
     * @param id advertisement identifier
     */
    @Override
    public void deleteAdvertisement(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));
        advertisement.setStatus(AdvertisementStatus.DELETED);
        advertisementRepository.save(advertisement);
    }


    /**
     * Retrieves an advertisement by its identifier.
     *
     * @param id advertisement identifier
     * @return advertisement information
     */
    @Override
    public AdvertisementDto getAdvertisementById(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));
        return mapToDto(advertisement);
    }

    /**
     * Retrieves all active advertisements using the default sorting.
     *
     * @return list of active advertisements
     */
    // 🟢 متد قدیمی برای حفظ ساختار کدهای قبلی پروژه
    @Override
    public List<AdvertisementDto> getAllActiveAdvertisement() {
        return getAllActiveAdvertisement("date", "desc");
    }


    /**
     * Retrieves all active advertisements with custom sorting.
     *
     * @param sortBy field used for sorting
     * @param order sorting direction
     * @return sorted list of advertisements
     */
    // 🟢 متد پیاده‌سازی شده جدید با قابلیت مرتب‌سازی پویای صفحه اول بازار
    @Override
    public List<AdvertisementDto> getAllActiveAdvertisement(String sortBy, String order) {
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort;

        if ("price".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(direction, "price");
        } else if ("rating".equalsIgnoreCase(sortBy)) {
            // برای رتبه‌بندی به دلیل منطق محاسباتی Average، مستقیماً از کوئری کاستوم دیتابیس کمک می‌گیریم
            List<Advertisement> ads = advertisementRepository.filterAdvertisementsAdvanced(
                    AdvertisementStatus.ACTIVE, null, null, null, null, null, "rating", order
            );
            return ads.stream().map(this::mapToDto).collect(Collectors.toList());
        } else {
            // حالت پیش‌فرض (مرتب‌سازی بر اساس تاریخ ثبت یعنی شناسه آگهی)
            sort = Sort.by(direction, "id");
        }

        return advertisementRepository.findByStatus(AdvertisementStatus.ACTIVE, sort).stream()
                .map(this::mapToDto)
                .toList();
    }


    /**
     * Searches advertisements using advanced filtering options.
     *
     * @param query search keyword
     * @param categoryId category identifier
     * @param cityId city identifier
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param sortBy sorting field
     * @param order sorting direction
     * @return matching advertisements
     */
    // 🟢 متد جدید برای جستجوی پیشرفته، فیلترها و مرتب‌سازی یکپارچه دیتابیس
    @Override
    public List<AdvertisementDto> searchAdvertisementsAdvanced(String query, Long categoryId, Long cityId, Double minPrice, Double maxPrice, String sortBy, String order) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;

        List<Advertisement> advertisements = advertisementRepository.filterAdvertisementsAdvanced(
                AdvertisementStatus.ACTIVE,
                cleanQuery,
                categoryId,
                cityId,
                minPrice,
                maxPrice,
                sortBy,
                order
        );

        return advertisements.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Approves an advertisement.
     *
     * @param id advertisement identifier
     * @return approved advertisement
     */
    @Override
    public AdvertisementDto approveAdvertisement(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));
        advertisement.setStatus(AdvertisementStatus.ACTIVE);
        advertisement.setRejectionReason(null);
        return mapToDto(advertisementRepository.save(advertisement));
    }

    /**
     * Searches advertisements by title.
     *
     * @param keyword search keyword
     * @return matching advertisements
     */
    @Override
    public List<AdvertisementDto> searchByTitle(String keyword) {
        return advertisementRepository
                .findByTitleContainingIgnoreCase(keyword)
                .stream()
                .filter(ad -> ad.getStatus() == AdvertisementStatus.ACTIVE)
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Rejects an advertisement.
     *
     * @param id advertisement identifier
     * @return rejected advertisement
     */
    @Override
    public AdvertisementDto rejectAdvertisement(Long id, String rejectionReason) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));

        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new IllegalArgumentException("A rejection reason is required");
        }

        advertisement.setStatus(AdvertisementStatus.REJECTED);
        advertisement.setRejectionReason(rejectionReason.trim());
        return mapToDto(advertisementRepository.save(advertisement));
    }

    /**
     * Retrieves all pending advertisements.
     *
     * @return list of pending advertisements
     */
    @Override
    public List<AdvertisementDto> getAllPendingAdvertisements() {
        return getAdvertisementsByStatus(AdvertisementStatus.PENDING);
    }

    /** Returns all advertisements for the administration workspace. */
    @Override
    public List<AdvertisementDto> getAllAdvertisements() {
        return advertisementRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    /** Returns advertisements in one explicit lifecycle state. */
    @Override
    public List<AdvertisementDto> getAdvertisementsByStatus(AdvertisementStatus status) {
        return advertisementRepository.findByStatus(status).stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Retrieves advertisements created by a specific seller.
     *
     * @param username seller username
     * @return seller advertisements
     */
    @Override
    public List<AdvertisementDto> getAdvertisementsBySellerUsername(String username) {
        User seller = userRepository.findByUserName(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        return advertisementRepository.findBySellerAndStatusNotOrderByIdDesc(seller, AdvertisementStatus.DELETED)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Broad categories are useful for navigation and search, but an ad must
     * always belong to a leaf so its displayed path is unambiguous.
     */
    private Category getLeafCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        if (categoryRepository.existsByParentId(category.getId())) {
            throw new IllegalArgumentException("Please select a subcategory for the advertisement");
        }

        return category;
    }

    private AdvertisementDto mapToDto(Advertisement advertisement) {
        return AdvertisementDto.builder()
                .id(advertisement.getId())
                .title(advertisement.getTitle())
                .description(advertisement.getDescription())
                .price(advertisement.getPrice())
                .status(advertisement.getStatus())
                .rejectionReason(advertisement.getRejectionReason())
                .sellerId(advertisement.getSeller().getId())
                .sellerName(advertisement.getSeller().getUserName())
                .categoryId(advertisement.getCategory().getId())
                .categoryName(advertisement.getCategory().getFullPath())
                .cityId(advertisement.getCity().getId())
                .cityName(advertisement.getCity().getName())
                .images(advertisement.getImages().stream()
                        .map(AdvertisementImage::getImagePath)
                        .toList())
                .imageDetails(advertisement.getImages().stream()
                        .map(this::mapImageToDto)
                        .toList())
                .build();
    }

    private AdvertisementImageDto mapImageToDto(AdvertisementImage image) {
        return AdvertisementImageDto.builder()
                .id(image.getId())
                .imagePath(image.getImagePath())
                .advertisementId(image.getAdvertisement().getId())
                .build();
    }

    /**
     * Marks an advertisement as sold.
     *
     * @param id advertisement identifier
     * @return updated advertisement
     */
    @Override
    public AdvertisementDto markAsSold(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));

        // فرض بر این است که SOLD در AdvertisementStatus تعریف شده است
        advertisement.setStatus(AdvertisementStatus.SOLD);
        return mapToDto(advertisementRepository.save(advertisement));
    }
}