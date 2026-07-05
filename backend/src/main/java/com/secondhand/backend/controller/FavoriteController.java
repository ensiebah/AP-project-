package com.secondhand.backend.controller;

import com.secondhand.backend.dto.FavoriteDto;
import com.secondhand.backend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public FavoriteDto addFavorite(
            @RequestParam Long userId,
            @RequestParam Long advertisementId
    ) {
        return favoriteService.addFavorite(
                userId,
                advertisementId
        );
    }

    @DeleteMapping
    public void removeFavorite(
            @RequestParam Long userId,
            @RequestParam Long advertisementId
    ) {
        favoriteService.removeFavorite(
                userId,
                advertisementId
        );
    }

    @GetMapping("/user/{userId}")
    public List<FavoriteDto> getUserFavorites(
            @PathVariable Long userId
    ) {
        return favoriteService.getUserFavorites(userId);
    }
}