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

    /**
     * Adds an advertisement to the authenticated user's favorites list.
     *
     * @param advertisementId the ID of the advertisement
     * @param principal the authenticated user
     * @return the created favorite entry
     */
    @PostMapping
    public FavoriteDto addFavorite(@RequestParam Long advertisementId, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return favoriteService.addFavorite(user.getId(), advertisementId);
    }


    /**
     * Removes an advertisement from the authenticated user's favorites list.
     *
     * @param advertisementId the ID of the advertisement
     * @param principal the authenticated user
     */
    @DeleteMapping
    public void removeFavorite(@RequestParam Long advertisementId, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        favoriteService.removeFavorite(user.getId(), advertisementId);
    }

    /**
     * Retrieves all favorite advertisements of the authenticated user.
     *
     * @param principal the authenticated user
     * @return a list of favorite advertisements
     */
    @GetMapping("/my-favorites")
    public List<FavoriteDto> getUserFavorites(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return favoriteService.getUserFavorites(user.getId());
    }
}