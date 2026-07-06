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

import java.util.ArrayList;
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
    public AdvertisementDto createAdvertisement(AdvertisementCreateDto dto, Long sellerId) {
        User seller = userRepository.findById(sellerId).orElseThrow(()->  new UserNotFoundException("seller not found")) ;
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(()->new RuntimeException("Category not found")) ;
        City city = cityRepository.findById(dto.getCityId()).orElseThrow(()->new RuntimeException("City not found")) ;

        Advertisement advertisement = new Advertisement() ;
        advertisement.setTitle(dto.getTitle());
        advertisement.setDescription(dto.getDescription());
        advertisement.setPrice(dto.getPrice());
        advertisement.setSeller(seller);
        advertisement.setCategory(category);
        advertisement.setCity(city);
        advertisement.setStatus(AdvertisementStatus.PENDING);

        // 👈 اصلاح اول: ذخیره‌سازی تصاویر در دیتابیس اگر کاربر عکسی فرستاده باشد
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<AdvertisementImage> imageEntities = dto.getImages().stream().map(path -> {
                AdvertisementImage img = new AdvertisementImage();
                img.setImagePath(path);
                img.setAdvertisement(advertisement); // برقراری رابطه دوطرفه
                return img;
            }).toList();
            advertisement.setImages(imageEntities);
        }

        Advertisement saved = advertisementRepository.save(advertisement) ;
        return mapToDto(saved) ;
    }

    @Override
    public AdvertisementDto updateAdvertisement(Long id, AdvertisementDto dto) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Advertisement not found")) ;
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        City city = cityRepository.findById(dto.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found"));

        advertisement.setTitle(dto.getTitle());
        advertisement.setDescription(dto.getDescription());
        advertisement.setPrice(dto.getPrice());
        advertisement.setCategory(category);
        advertisement.setCity(city);

        // 👈 اصلاح دوم: آپدیت تصاویر در صورت ارسال لیست جدید
        if (dto.getImages() != null) {
            advertisement.getImages().clear(); // پاک کردن تصاویر قبلی (بخاطر CascadeType.ALL خودکار حذف می‌شوند)
            List<AdvertisementImage> newImages = dto.getImages().stream().map(path -> {
                AdvertisementImage img = new AdvertisementImage();
                img.setImagePath(path);
                img.setAdvertisement(advertisement);
                return img;
            }).toList();
            advertisement.getImages().addAll(newImages);
        }

        Advertisement updated = advertisementRepository.save(advertisement) ;
        return mapToDto(updated) ;
    }

    @Override
    public void deleteAdvertisement(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id).orElseThrow(()->new RuntimeException("Advertisement not found")) ;
        advertisement.setStatus(AdvertisementStatus.DELETED);
        advertisementRepository.save(advertisement) ;
    }

    @Override
    public AdvertisementDto getAdvertisementById(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id).orElseThrow(()->new RuntimeException("Advertisement not found")) ;
        return mapToDto(advertisement);
    }

    @Override
    public List<AdvertisementDto> getAllActiveAdvertisement() {
        return advertisementRepository.findByStatus(AdvertisementStatus.ACTIVE).stream()
                .map(this::mapToDto)
                .toList();
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
    public AdvertisementDto approveAdvertisement(Long id) {
        Advertisement advertisement  =  advertisementRepository.findById(id).orElseThrow(()->new RuntimeException("Advertisement not found")) ;
        advertisement.setStatus(AdvertisementStatus.ACTIVE);
        return mapToDto(advertisementRepository.save(advertisement)) ;
    }

    @Override
    public AdvertisementDto rejectAdvertisement(Long id) {
        Advertisement advertisement =
                advertisementRepository.findById(id).orElseThrow(()->new RuntimeException("Advertisement not found")) ;
        advertisement.setStatus(AdvertisementStatus.REJECTED);
        return mapToDto(advertisementRepository.save(advertisement)) ;
    }

    @Override
    public List<AdvertisementDto> searchAndFilter(com.secondhand.backend.dto.FilterAdvertisementDto filterDto) {
        List<Advertisement> advertisements = advertisementRepository.filterAdvertisements(
                filterDto.getQuery(),
                filterDto.getCategoryId(),
                filterDto.getCityId(),
                filterDto.getMinPrice(),
                filterDto.getMaxPrice()
        );

        return advertisements.stream()
                .filter(ad -> ad.getStatus() == AdvertisementStatus.ACTIVE)
                .map(this::mapToDto)
                .toList();
    }

    private AdvertisementDto mapToDto(Advertisement advertisement){
        // 👈 اصلاح سوم: استخراج لیست مسیر تصاویر از انتیتی و تبدیل به آرایه‌ای از String
        List<String> imagePaths = new ArrayList<>();
        if (advertisement.getImages() != null) {
            imagePaths = advertisement.getImages().stream()
                    .map(AdvertisementImage::getImagePath)
                    .toList();
        }

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
                .images(imagePaths) // 👈 اضافه شدن لیست مسیر تصاویر به خروجی نهایی DTO
                .build() ;
    }
}