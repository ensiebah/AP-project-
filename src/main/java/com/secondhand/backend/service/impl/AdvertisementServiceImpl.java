package com.secondhand.backend.service.impl;

import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.secondhand.backend.dto.AdvertisementCreateDto;
import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.dto.AdvertisementUpdateDto;

import com.secondhand.backend.entity.AdvertisementStatus;
import com.secondhand.backend.entity.Category;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.entity.User;

import com.secondhand.backend.exception.AdvertisementNotFoundException;
import com.secondhand.backend.exception.CategoryNotFoundException;
import com.secondhand.backend.exception.CityNotFoundException;
import com.secondhand.backend.exception.UnauthorizedAdvertisementAccessException;
import com.secondhand.backend.exception.UserNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;

    @Override
    public AdvertisementDto createAdvertisement(
            Long sellerId,
            AdvertisementCreateDto request
    ) {

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        Category category = categoryRepository.findById(
                request.getCategoryId()
        ).orElseThrow(() ->
                new CategoryNotFoundException("Category not found"));

        City city = cityRepository.findById(
                request.getCityId()
        ).orElseThrow(() ->
                new CityNotFoundException("City not found"));

        Advertisement advertisement = new Advertisement();

        advertisement.setTitle(request.getTitle());
        advertisement.setDescription(request.getDescription());
        advertisement.setPrice(request.getPrice());

        advertisement.setSeller(seller);
        advertisement.setCategory(category);
        advertisement.setCity(city);

        advertisement.setStatus(AdvertisementStatus.PENDING);

        Advertisement savedAdvertisement =
                advertisementRepository.save(advertisement);

        return mapToDto(savedAdvertisement);
    }

    @Override
    public AdvertisementDto updateAdvertisement(
            Long advertisementId,
            Long sellerId,
            AdvertisementUpdateDto request
    ) {

        Advertisement advertisement =
                advertisementRepository.findById(advertisementId)
                        .orElseThrow(() ->
                                new AdvertisementNotFoundException(
                                        "Advertisement not found"));

        if (!advertisement.getSeller()
                .getId()
                .equals(sellerId)) {

            throw new UnauthorizedAdvertisementAccessException(
                    "You are not owner of this advertisement");
        }

        Category category = categoryRepository.findById(
                request.getCategoryId()
        ).orElseThrow(() ->
                new CategoryNotFoundException("Category not found"));

        City city = cityRepository.findById(
                request.getCityId()
        ).orElseThrow(() ->
                new CityNotFoundException("City not found"));

        advertisement.setTitle(request.getTitle());
        advertisement.setDescription(request.getDescription());
        advertisement.setPrice(request.getPrice());

        advertisement.setCategory(category);
        advertisement.setCity(city);

        Advertisement savedAdvertisement =
                advertisementRepository.save(advertisement);

        return mapToDto(savedAdvertisement);
    }

    @Override
    public void deleteAdvertisement(
            Long advertisementId,
            Long sellerId
    ) {

        Advertisement advertisement =
                advertisementRepository.findById(advertisementId)
                        .orElseThrow(() ->
                                new AdvertisementNotFoundException(
                                        "Advertisement not found"));

        if (!advertisement.getSeller()
                .getId()
                .equals(sellerId)) {

            throw new UnauthorizedAdvertisementAccessException(
                    "You are not owner of this advertisement");
        }

        advertisement.setStatus(AdvertisementStatus.DELETED);

        advertisementRepository.save(advertisement);
    }

    private AdvertisementDto mapToDto(
            Advertisement advertisement
    ) {

        return AdvertisementDto.builder()
                .id(advertisement.getId())
                .title(advertisement.getTitle())
                .description(advertisement.getDescription())
                .price(advertisement.getPrice())

                .status(advertisement.getStatus())

                .sellerId(
                        advertisement.getSeller().getId()
                )
                .sellerName(
                        advertisement.getSeller().getUserName()
                )

                .categoryId(
                        advertisement.getCategory().getId()
                )
                .categoryName(
                        advertisement.getCategory().getName()
                )

                .cityId(
                        advertisement.getCity().getId()
                )
                .cityName(
                        advertisement.getCity().getName()
                )

                .build();
    }

    @Override
    public AdvertisementDto getAdvertisementById(
            Long advertisementId
    ) {

        Advertisement advertisement =
                advertisementRepository.findById(advertisementId)
                        .orElseThrow(() ->
                                new AdvertisementNotFoundException(
                                        "Advertisement not found"));

        return mapToDto(advertisement);
    }

    @Override
    public List<AdvertisementDto> getAllActiveAdvertisement() {

        return advertisementRepository
                .findByStatus(AdvertisementStatus.ACTIVE)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<AdvertisementDto> searchByTitle(
            String title
    ) {

        return advertisementRepository
                .findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public void approveAdvertisement(
            Long advertisementId
    ) {

        Advertisement advertisement =
                advertisementRepository.findById(advertisementId)
                        .orElseThrow(() ->
                                new AdvertisementNotFoundException(
                                        "Advertisement not found"));

        advertisement.setStatus(
                AdvertisementStatus.ACTIVE
        );

        advertisementRepository.save(advertisement);
    }

    @Override
    public void rejectAdvertisement(
            Long advertisementId
    ) {

        Advertisement advertisement =
                advertisementRepository.findById(advertisementId)
                        .orElseThrow(() ->
                                new AdvertisementNotFoundException(
                                        "Advertisement not found"));

        advertisement.setStatus(
                AdvertisementStatus.REJECTED
        );

        advertisementRepository.save(advertisement);
    }


}