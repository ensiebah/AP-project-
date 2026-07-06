package com.secondhand.backend.controller;

import com.secondhand.backend.dto.LoginRequestDto;
import com.secondhand.backend.dto.LoginResponseDto;
import com.secondhand.backend.dto.RegisterRequestDto;
import com.secondhand.backend.dto.UserDto;
import com.secondhand.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserDto register(@RequestBody RegisterRequestDto request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto request) {
        return userService.login(request);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/username/{username}")
    public UserDto getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}/block")
    public void blockUser(@PathVariable Long id) {
        userService.blockUser(id);
    }

    @PutMapping("/{id}/unblock")
    public void unblockUser(@PathVariable Long id) {
        userService.unblockUser(id);
    }
}