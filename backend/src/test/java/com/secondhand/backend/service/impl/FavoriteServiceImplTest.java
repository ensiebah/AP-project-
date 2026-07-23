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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdvertisementRepository advertisementRepository;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    private User user;
    private Advertisement advertisement;
    private Favorite favorite;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);

        advertisement = new Advertisement();
        advertisement.setId(10L);

        favorite = new Favorite();
        favorite.setId(100L);
        favorite.setUser(user);
        favorite.setAdvertisement(advertisement);
    }

    @Test
    void addFavoriteSuccess() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(advertisementRepository.findById(10L))
                .thenReturn(Optional.of(advertisement));

        when(favoriteRepository.existsByUserAndAdvertisement(user, advertisement))
                .thenReturn(false);

        when(favoriteRepository.save(any(Favorite.class)))
                .thenReturn(favorite);

        FavoriteDto dto = favoriteService.addFavorite(1L,10L);

        assertNotNull(dto);
        assertEquals(1L,dto.getUserId());
        assertEquals(10L,dto.getAdvertisementId());

        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    void addFavoriteUserNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                ()->favoriteService.addFavorite(1L,10L));
    }

    @Test
    void addFavoriteAdvertisementNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(advertisementRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                ()->favoriteService.addFavorite(1L,10L));
    }

    @Test
    void addFavoriteAlreadyExists() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(advertisementRepository.findById(10L))
                .thenReturn(Optional.of(advertisement));

        when(favoriteRepository.existsByUserAndAdvertisement(user, advertisement))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                ()->favoriteService.addFavorite(1L,10L));
    }

    @Test
    void removeFavoriteSuccess() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(advertisementRepository.findById(10L))
                .thenReturn(Optional.of(advertisement));

        when(favoriteRepository.findByUserAndAdvertisement(user,advertisement))
                .thenReturn(Optional.of(favorite));

        favoriteService.removeFavorite(1L,10L);

        verify(favoriteRepository).delete(favorite);
    }

    @Test
    void removeFavoriteNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(advertisementRepository.findById(10L))
                .thenReturn(Optional.of(advertisement));

        when(favoriteRepository.findByUserAndAdvertisement(user,advertisement))
                .thenReturn(Optional.empty());

        assertThrows(FavoriteNotFoundException.class,
                ()->favoriteService.removeFavorite(1L,10L));
    }

    @Test
    void getUserFavoritesSuccess() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(favoriteRepository.findByUser(user))
                .thenReturn(List.of(favorite));

        List<FavoriteDto> list = favoriteService.getUserFavorites(1L);

        assertEquals(1,list.size());
        assertEquals(10L,list.get(0).getAdvertisementId());
    }

}