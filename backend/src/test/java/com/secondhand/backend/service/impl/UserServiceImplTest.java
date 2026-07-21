package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.*;
import com.secondhand.backend.entity.Role;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.DuplicateEmailException;
import com.secondhand.backend.exception.DuplicateUsernameException;
import com.secondhand.backend.exception.InvalidLoginException;
import com.secondhand.backend.exception.UserNotFoundException;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtUtil jwtUtil;

    @InjectMocks
    UserServiceImpl userService;

    private User user;
    private RegisterRequestDto registerDto;
    private LoginRequestDto loginDto;

    @BeforeEach
    void setup() {

        user = new User();
        user.setId(1L);
        user.setUserName("ali");
        user.setPassWord("encodedPassword");
        user.setFullName("Ali Ahmadi");
        user.setEmail("ali@gmail.com");
        user.setPhoneNumber("09123456789");
        user.setRole(Role.USER);
        user.setBlocked(false);

        registerDto = RegisterRequestDto.builder()
                .fullName("Ali Ahmadi")
                .username("ali")
                .password("1234")
                .email("ali@gmail.com")
                .phone("09123456789")
                .build();

        loginDto = LoginRequestDto.builder()
                .username("ali")
                .password("1234")
                .build();
    }

    @Test
    void registerSuccess() {

        when(userRepository.existsByUserName("ali")).thenReturn(false);
        when(userRepository.existsByEmail("ali@gmail.com")).thenReturn(false);

        when(passwordEncoder.encode("1234"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserDto result = userService.register(registerDto);

        assertNotNull(result);
        assertEquals("ali", result.getUsername());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerDuplicateUsername() {

        when(userRepository.existsByUserName("ali"))
                .thenReturn(true);

        assertThrows(
                DuplicateUsernameException.class,
                () -> userService.register(registerDto)
        );
    }

    @Test
    void registerDuplicateEmail() {

        when(userRepository.existsByUserName("ali"))
                .thenReturn(false);

        when(userRepository.existsByEmail("ali@gmail.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateEmailException.class,
                () -> userService.register(registerDto)
        );
    }

    @Test
    void loginSuccess() {

        when(userRepository.findByUserName("ali"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("1234", "encodedPassword"))
                .thenReturn(true);

        when(jwtUtil.generateToken("ali", "USER"))
                .thenReturn("jwt-token");

        LoginResponseDto response = userService.login(loginDto);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("ali", response.getUsername());
    }

    @Test
    void loginWrongUsername() {

        when(userRepository.findByUserName("ali"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidLoginException.class,
                () -> userService.login(loginDto)
        );
    }

    @Test
    void loginWrongPassword() {

        when(userRepository.findByUserName("ali"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("1234", "encodedPassword"))
                .thenReturn(false);

        assertThrows(
                InvalidLoginException.class,
                () -> userService.login(loginDto)
        );
    }

    @Test
    void loginBlockedUser() {

        user.setBlocked(true);

        when(userRepository.findByUserName("ali"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("1234", "encodedPassword"))
                .thenReturn(true);

        assertThrows(
                InvalidLoginException.class,
                () -> userService.login(loginDto)
        );
    }

    @Test
    void getUserByIdSuccess() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserDto dto = userService.getUserById(1L);

        assertEquals("ali", dto.getUsername());
    }

    @Test
    void getUserByIdNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(1L)
        );
    }

    @Test
    void blockUserSuccess() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.blockUser(1L);

        assertTrue(user.isBlocked());

        verify(userRepository).save(user);
    }

    @Test
    void unblockUserSuccess() {

        user.setBlocked(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.unblockUser(1L);

        assertFalse(user.isBlocked());

        verify(userRepository).save(user);
    }
}