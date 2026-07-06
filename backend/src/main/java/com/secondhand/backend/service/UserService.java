package com.secondhand.backend.service;

import com.secondhand.backend.dto.LoginRequestDto;
import com.secondhand.backend.dto.LoginResponseDto;
import com.secondhand.backend.dto.RegisterRequestDto;
import com.secondhand.backend.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto register(RegisterRequestDto request);

    LoginResponseDto login(LoginRequestDto request);

    UserDto getUserById(Long id);

    UserDto getUserByUsername(String username);

    List<UserDto> getAllUsers();

    void blockUser(Long userId);

    void unblockUser(Long userId);
}