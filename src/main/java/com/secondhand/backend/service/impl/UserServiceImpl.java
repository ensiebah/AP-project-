package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.LoginRequestDto;
import com.secondhand.backend.dto.LoginResponseDto;
import com.secondhand.backend.dto.RegisterRequestDto;
import com.secondhand.backend.dto.UserDto;
import com.secondhand.backend.entity.Role;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.BlockedUserException;
import com.secondhand.backend.exception.DuplicateEmailException;
import com.secondhand.backend.exception.DuplicateUsernameException;
import com.secondhand.backend.exception.InvalidLoginException;
import com.secondhand.backend.exception.UserNotFoundException;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto register(RegisterRequestDto request) {

        if (userRepository.existsByUserName(request.getUsername())) {
            throw new DuplicateUsernameException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setUserName(request.getUsername());
        user.setPassWord(request.getPassword());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhone());

        user.setRole(Role.USER);
        user.setBlocked(false);

        User savedUser = userRepository.save(user);

        return mapToDto(savedUser);
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {

        User user = userRepository.findByUserName(request.getUsername())
                .orElseThrow(() ->
                        new InvalidLoginException("Invalid username or password"));

        if (!user.getPassWord().equals(request.getPassword())) {
            throw new InvalidLoginException("Invalid username or password");
        }

        if (user.isBlocked()) {
            throw new BlockedUserException("User is blocked");
        }

        return LoginResponseDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUserName())
                .role(user.getRole())
                .build();
    }

    @Override
    public UserDto getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        return mapToDto(user);
    }

    @Override
    public UserDto getUserByUsername(String username) {

        User user = userRepository.findByUserName(username)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        return mapToDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public void blockUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        user.setBlocked(true);

        userRepository.save(user);
    }

    @Override
    public void unblockUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        user.setBlocked(false);

        userRepository.save(user);
    }

    private UserDto mapToDto(User user) {

        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .role(user.getRole())
                .isBlocked(user.isBlocked())
                .build();
    }
}