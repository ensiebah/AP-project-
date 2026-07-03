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
        // ۱. استفاده از حروف کوچک طبق DTO شما (request.getUsername)
        if (userRepository.existsByUserName(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // ۲. رمزنگاری پسورد ورودی (password با حروف کوچک)
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // ساخت و پر کردن فیلدهای انتیتی کاربر
        User user = new User();
        user.setUserName(request.getUsername());
        user.setPassWord(hashedPassword);
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhone());
        user.setRole(Role.USER); // نقش پیش‌فرض طبق داک پروژه
        user.setBlocked(false);  // وضعیت پیش‌فرض

        // ۳. استفاده از متد mapToDto خودتان که از قبل موجود بود
        return mapToDto(userRepository.save(user));
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByUserName(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassWord())) {
            throw new RuntimeException("Invalid password");
        }

        if (user.isBlocked()) {
            throw new RuntimeException("User is blocked");
        }

        // 👈 تولید توکن واقعی با استفاده از نام کاربری و نام نقش کاربر
        String token = jwtUtil.generateToken(user.getUserName(), user.getRole().name());

        // ساخت ریسپانس با استفاده از بیلدر دی‌تی‌او همراه با توکن واقعی
        return LoginResponseDto.builder()
                .id(user.getId())
                .username(user.getUserName())
                .fullName(user.getFullName())
                .role(user.getRole())
                .token(token) // 👈 توکن واقعی اینجا ست می‌شود
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

    @Override
    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setBlocked(true);
        userRepository.save(user);
    }

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
                .isBlocked(user.isBlocked()) // بدون خطا همگام شد
                .build();
    }
}