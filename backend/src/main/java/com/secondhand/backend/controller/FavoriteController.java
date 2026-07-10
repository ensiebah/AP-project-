package com.secondhand.backend.controller;

import com.secondhand.backend.dto.FavoriteDto;
import com.secondhand.backend.service.FavoriteService;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    @PostMapping
    public FavoriteDto addFavorite(@RequestParam Long advertisementId, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return favoriteService.addFavorite(user.getId(), advertisementId);
    }

    @DeleteMapping
    public void removeFavorite(@RequestParam Long advertisementId, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        favoriteService.removeFavorite(user.getId(), advertisementId);
    }

    @GetMapping("/my-favorites")
    public List<FavoriteDto> getUserFavorites(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return favoriteService.getUserFavorites(user.getId());
    }
}