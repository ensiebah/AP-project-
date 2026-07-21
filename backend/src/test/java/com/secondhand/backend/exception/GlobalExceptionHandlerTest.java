package com.secondhand.backend.exception;

import com.secondhand.backend.controller.UserController;
import com.secondhand.backend.dto.LoginRequestDto;
import com.secondhand.backend.dto.RegisterRequestDto;
import com.secondhand.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @Test
    void login_ShouldReturnError_WhenInvalidLogin() throws Exception {

        LoginRequestDto dto = LoginRequestDto.builder()
                .username("ali")
                .password("1234")
                .build();

        doThrow(new InvalidLoginException("Wrong username or password"))
                .when(userService)
                .login(any(LoginRequestDto.class));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("ERROR|Wrong username or password"));
    }

    @Test
    void register_ShouldReturnDuplicateUsername() throws Exception {

        RegisterRequestDto dto = RegisterRequestDto.builder()
                .fullName("Ali")
                .username("ali")
                .password("1234")
                .email("ali@gmail.com")
                .phone("09123456789")
                .build();

        doThrow(new DuplicateUsernameException("Username already exists"))
                .when(userService)
                .register(any(RegisterRequestDto.class));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("ERROR|Username already exists"));
    }

    @Test
    void register_ShouldReturnDuplicateEmail() throws Exception {

        RegisterRequestDto dto = RegisterRequestDto.builder()
                .fullName("Ali")
                .username("ali")
                .password("1234")
                .email("ali@gmail.com")
                .phone("09123456789")
                .build();

        doThrow(new DuplicateEmailException("Email already exists"))
                .when(userService)
                .register(any(RegisterRequestDto.class));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("ERROR|Email already exists"));
    }

    @Test
    void register_ShouldReturnValidationError() throws Exception {

        RegisterRequestDto dto = RegisterRequestDto.builder()
                .fullName("")
                .username("")
                .password("")
                .email("wrong-email")
                .phone("123")
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("ERROR|")));
    }
}