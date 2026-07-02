package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.FavoriteDto;

import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.entity.Favorite;
import com.secondhand.backend.entity.User;

import com.secondhand.backend.exception.AdvertisementNotFoundException;
import com.secondhand.backend.exception.FavoriteNotFoundException;
import com.secondhand.backend.exception.UserNotFoundException;

import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.repository.FavoriteRepository;
import com.secondhand.backend.repository.UserRepository;

import com.secondhand.backend.service.FavoriteService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final AdvertisementRepository advertisementRepository;

    @Override
    public FavoriteDto addFavorite(
            Long userId,
            Long advertisementId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"));

        Advertisement advertisement =
                advertisementRepository.findById(advertisementId)
                        .orElseThrow(() ->
                                new AdvertisementNotFoundException(
                                        "Advertisement not found"));

        if (favoriteRepository.existsByUserAndAdvertisement(
                user,
                advertisement
        )) {

            throw new IllegalArgumentException(
                    "Advertisement already in favorites");
        }

        Favorite favorite = new Favorite();

        favorite.setUser(user);
        favorite.setAdvertisement(advertisement);

        Favorite savedFavorite =
                favoriteRepository.save(favorite);

        return mapToDto(savedFavorite);
    }

    @Override
    public void removeFavorite(
            Long userId,
            Long advertisementId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"));

        Advertisement advertisement =
                advertisementRepository.findById(advertisementId)
                        .orElseThrow(() ->
                                new AdvertisementNotFoundException(
                                        "Advertisement not found"));

        Favorite favorite =
                favoriteRepository
                        .findByUserAndAdvertisement(
                                user,
                                advertisement
                        )
                        .orElseThrow(() ->
                                new FavoriteNotFoundException(
                                        "Favorite not found"));

        favoriteRepository.delete(favorite);
    }

    @Override
    public List<FavoriteDto> getUserFavorites(
            Long userId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"));

        return favoriteRepository.findByUser(user)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private FavoriteDto mapToDto(
            Favorite favorite
    ) {

        return FavoriteDto.builder()
                .id(favorite.getId())

                .userId(
                        favorite.getUser().getId()
                )

                .advertisementId(
                        favorite.getAdvertisement().getId()
                )

                .build();
    }
    }