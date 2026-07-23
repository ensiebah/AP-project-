package com.secondhand.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.backend.dto.LoginRequestDto;
import com.secondhand.backend.dto.LoginResponseDto;
import com.secondhand.backend.dto.RegisterRequestDto;
import com.secondhand.backend.dto.UserDto;
import com.secondhand.backend.entity.Role;
import com.secondhand.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
@EnableMethodSecurity
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void register_ShouldReturnUser() throws Exception {

        RegisterRequestDto request = RegisterRequestDto.builder()
                .fullName("Ali")
                .username("ali")
                .password("1234")
                .email("ali@test.com")
                .phone("09123456789")
                .build();

        UserDto response = UserDto.builder()
                .id(1L)
                .username("ali")
                .fullName("Ali")
                .email("ali@test.com")
                .role(Role.USER)
                .build();

        when(userService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ali"));

        verify(userService).register(any());
    }

    @Test
    void login_ShouldReturnToken() throws Exception {

        LoginRequestDto request = LoginRequestDto.builder()
                .username("ali")
                .password("1234")
                .build();

        LoginResponseDto response = LoginResponseDto.builder()
                .id(1L)
                .username("ali")
                .token("jwt-token")
                .role(Role.USER)
                .build();

        when(userService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(userService).login(any());
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {

        UserDto dto = UserDto.builder()
                .id(1L)
                .username("ali")
                .build();

        when(userService.getUserById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ali"));

        verify(userService).getUserById(1L);
    }

    @Test
    void getUserByUsername_ShouldReturnUser() throws Exception {

        UserDto dto = UserDto.builder()
                .id(1L)
                .username("ali")
                .build();

        when(userService.getUserByUsername("ali")).thenReturn(dto);

        mockMvc.perform(get("/api/users/username/ali"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ali"));

        verify(userService).getUserByUsername("ali");
    }

    @Test
    void getAllUsers_ShouldReturnList() throws Exception {

        UserDto dto = UserDto.builder()
                .id(1L)
                .username("ali")
                .build();

        when(userService.getAllUsers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("ali"));

        verify(userService).getAllUsers();
    }

    @Test
    void blockUser_ShouldReturnOk() throws Exception {

        doNothing().when(userService).blockUser(1L);

        mockMvc.perform(put("/api/users/admin/users/1/block"))
                .andExpect(status().isOk());

        verify(userService).blockUser(1L);
    }

    @Test
    void unblockUser_ShouldReturnOk() throws Exception {

        doNothing().when(userService).unblockUser(1L);

        mockMvc.perform(put("/api/users/admin/users/1/unblock"))
                .andExpect(status().isOk());

        verify(userService).unblockUser(1L);
    }
}