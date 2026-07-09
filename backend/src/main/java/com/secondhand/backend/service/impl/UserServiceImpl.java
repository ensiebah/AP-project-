package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.LoginRequestDto;
import com.secondhand.backend.dto.LoginResponseDto;
import com.secondhand.backend.dto.RegisterRequestDto;
import com.secondhand.backend.dto.UserDto;
import com.secondhand.backend.entity.Role;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.security.JwtUtil;
import com.secondhand.backend.service.UserService;
import com.secondhand.backend.exception.UserNotFoundException;
import com.secondhand.backend.exception.InvalidLoginException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserDto register(RegisterRequestDto request) {
        if (userRepository.existsByUserName(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setUserName(request.getUsername());
        user.setPassWord(hashedPassword);
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhone());
        user.setRole(Role.USER);
        user.setBlocked(false);

        return mapToDto(userRepository.save(user));
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByUserName(request.getUsername())
                .orElseThrow(() -> new InvalidLoginException("Login failed. This username is not registered yet."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassWord())) {
            throw new InvalidLoginException("Incorrect password. Please try again.");
        }

        // 🟢 Check if the user account is flagged as blocked in the database
        if (user.isBlocked()) {
            throw new InvalidLoginException("Your account has been blocked by the administrator.");
        }

        String token = jwtUtil.generateToken(user.getUserName(), user.getRole().name());

        return LoginResponseDto.builder()
                .id(user.getId())
                .username(user.getUserName())
                .fullName(user.getFullName())
                .role(user.getRole())
                .token(token)
                .build();
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return mapToDto(user);
    }

    @Override
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return mapToDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * 🟢 Responsibility: Updates the database status to flag a specific user as blocked.
     */
    @Override
    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setBlocked(true);
        userRepository.save(user);
    }

    /**
     * 🟢 Responsibility: Updates the database status to unblock/activate a specific user.
     */
    @Override
    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
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