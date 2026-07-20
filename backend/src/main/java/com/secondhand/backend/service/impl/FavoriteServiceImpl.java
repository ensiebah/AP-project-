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

/**
 * Service implementation responsible for managing
 * users' favorite advertisements.
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final AdvertisementRepository advertisementRepository;


    /**
     * Adds an advertisement to the user's favorites.
     *
     * @param userId user identifier
     * @param advertisementId advertisement identifier
     * @return created favorite
     */
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


    /**
     * Removes an advertisement from the user's favorites.
     *
     * @param userId user identifier
     * @param advertisementId advertisement identifier
     */
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



    /**
     * Retrieves all favorite advertisements of a user.
     *
     * @param userId user identifier
     * @return list of favorites
     */
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