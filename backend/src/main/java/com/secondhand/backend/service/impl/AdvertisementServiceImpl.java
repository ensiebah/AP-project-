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

    /**
     * 🟢 وظیفه: متد جدید برای ساخت آگهی بر اساس نام کاربری استخراج شده از JWT
     * 🔍 نقشه ذهنی: ابتدا کاربر را بر اساس یوزرنیم یکتایش پیدا می‌کند،
     * سپس صحت وجود دسته‌بندی و شهر را بررسی کرده و آگهی را با وضعیت PENDING ذخیره می‌کند.
     */
    public AdvertisementDto createAdvertisementByUsername(AdvertisementCreateDto dto, String username) {
        // پیدا کردن کاربر ثبت‌کننده آگهی از روی نام کاربری دیتابیس
        User seller = userRepository.findByUserName(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        City city = cityRepository.findById(dto.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found"));

        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(dto.getTitle());
        advertisement.setDescription(dto.getDescription());
        advertisement.setPrice(dto.getPrice());
        advertisement.setSeller(seller);
        advertisement.setCategory(category);
        advertisement.setCity(city);
        advertisement.setStatus(AdvertisementStatus.PENDING); // طبق داک، ابتدا در انتظار تایید است

        Advertisement saved = advertisementRepository.save(advertisement);
        return mapToDto(saved);
    }

    // این متد قدیمی را جهت سازگاری با اینترفیس حفظ می‌کنیم
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
                .filter(ad->ad.getStatus()== AdvertisementStatus.ACTIVE)
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

    /**
     * 🔄 وظیفه: تبدیل انتیتی پایگاه داده (Entity) به دی‌تی‌او خروجی (DTO) برای ارسال به فرانت‌آند
     */
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