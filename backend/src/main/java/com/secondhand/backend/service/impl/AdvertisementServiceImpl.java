package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.AdvertisementCreateDto;
import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.entity.*;
import com.secondhand.backend.exception.UserNotFoundException;
import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.AdvertisementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;

    @Override
    public AdvertisementDto createAdvertisementByUsername(AdvertisementCreateDto dto, String username) {
        User seller = userRepository.findByUserName(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setId(dto.getCategoryId());
                    newCategory.setName("Category " + dto.getCategoryId());
                    return categoryRepository.save(newCategory);
                });

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

    @Override
    public AdvertisementDto createAdvertisement(AdvertisementCreateDto dto, Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException("seller not found"));
        return createAdvertisementByUsername(dto, seller.getUserName());
    }

    @Override
    public AdvertisementDto updateAdvertisement(Long id, AdvertisementDto dto) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

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

    @Override
    public void deleteAdvertisement(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));
        advertisement.setStatus(AdvertisementStatus.DELETED);
        advertisementRepository.save(advertisement);
    }

    @Override
    public AdvertisementDto getAdvertisementById(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));
        return mapToDto(advertisement);
    }

    @Override
    public List<AdvertisementDto> getAllActiveAdvertisement() {
        return advertisementRepository.findByStatus(AdvertisementStatus.ACTIVE).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public AdvertisementDto approveAdvertisement(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));
        advertisement.setStatus(AdvertisementStatus.ACTIVE);
        return mapToDto(advertisementRepository.save(advertisement));
    }

    @Override
    public List<AdvertisementDto> searchByTitle(String keyword) {
        return advertisementRepository
                .findByTitleContainingIgnoreCase(keyword)
                .stream()
                .filter(ad -> ad.getStatus() == AdvertisementStatus.ACTIVE)
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public AdvertisementDto rejectAdvertisement(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));
        advertisement.setStatus(AdvertisementStatus.REJECTED);
        return mapToDto(advertisementRepository.save(advertisement));
    }

    @Override
    public List<AdvertisementDto> getAllPendingAdvertisements() {
        return advertisementRepository.findByStatus(AdvertisementStatus.PENDING).stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Fetches all personal advertisements belonging to the seller, filtered to exclude DELETED status, ordered by newest.
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

    private AdvertisementDto mapToDto(Advertisement advertisement) {
        return AdvertisementDto.builder()
                .id(advertisement.getId())
                .title(advertisement.getTitle())
                .description(advertisement.getDescription())
                .price(advertisement.getPrice())
                .status(advertisement.getStatus())
                .sellerId(advertisement.getSeller().getId())
                .sellerName(advertisement.getSeller().getUserName())
                .categoryId(advertisement.getCategory().getId())
                .categoryName(advertisement.getCategory().getName())
                .cityId(advertisement.getCity().getId())
                .cityName(advertisement.getCity().getName())
                .build();
    }
}