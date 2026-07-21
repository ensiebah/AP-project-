package com.secondhand.backend.controller;

import com.secondhand.backend.dto.FavoriteDto;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.FavoriteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoriteController.class)
@AutoConfigureMockMvc(addFilters = false)
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoriteService favoriteService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void addFavorite_ShouldReturnFavorite() throws Exception {

        User user = new User();
        user.setId(1L);
        user.setUserName("ali");

        FavoriteDto dto = FavoriteDto.builder()
                .id(1L)
                .userId(1L)
                .advertisementId(10L)
                .build();

        when(userRepository.findByUserName("ali"))
                .thenReturn(Optional.of(user));

        when(favoriteService.addFavorite(1L,10L))
                .thenReturn(dto);

        Principal principal = () -> "ali";

        mockMvc.perform(post("/api/favorites")
                        .principal(principal)
                        .param("advertisementId","10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.advertisementId").value(10));
    }

    @Test
    void getFavorites_ShouldReturnList() throws Exception {

        User user = new User();
        user.setId(1L);
        user.setUserName("ali");

        FavoriteDto dto = FavoriteDto.builder()
                .id(1L)
                .userId(1L)
                .advertisementId(20L)
                .build();

        when(userRepository.findByUserName("ali"))
                .thenReturn(Optional.of(user));

        when(favoriteService.getUserFavorites(anyLong()))
                .thenReturn(List.of(dto));

        Principal principal = () -> "ali";

        mockMvc.perform(get("/api/favorites/my-favorites")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].advertisementId").value(20));
    }

    @Test
    void removeFavorite_ShouldReturnOk() throws Exception {

        User user = new User();
        user.setId(1L);
        user.setUserName("ali");

        when(userRepository.findByUserName("ali"))
                .thenReturn(Optional.of(user));

        Principal principal = () -> "ali";

        mockMvc.perform(delete("/api/favorites")
                        .principal(principal)
                        .param("advertisementId","20"))
                .andExpect(status().isOk());

        verify(favoriteService).removeFavorite(1L,20L);
    }

}