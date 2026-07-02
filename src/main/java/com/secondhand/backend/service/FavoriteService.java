package com.secondhand.backend.service;

import com.secondhand.backend.dto.FavoriteDto;

import java.util.List;

public interface FavoriteService {

    FavoriteDto addFavorite(
            Long userId,
            Long advertisementId
    );

    void removeFavorite(
            Long userId,
            Long advertisementId
    );

    List<FavoriteDto> getUserFavorites(
            Long userId
    );
}