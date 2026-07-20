package com.secondhand.backend.controller;

import com.secondhand.backend.dto.LoginRequestDto;
import com.secondhand.backend.dto.LoginResponseDto;
import com.secondhand.backend.dto.RegisterRequestDto;
import com.secondhand.backend.dto.UserDto;
import com.secondhand.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Registers a new user account.
     *
     * @param request the registration information
     * @return the registered user information
     */
    @PostMapping("/register")
    public UserDto register(@Valid @RequestBody RegisterRequestDto request) {
        return userService.register(request);
    }


    /**
     * Authenticates a user and returns a JWT token
     * along with the user's basic information.
     *
     * @param request the user's login credentials
     * @return the authentication result
     */
    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto request) {
        return userService.login(request);
    }


    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id the user ID
     * @return the requested user
     */
    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }


    /**
     * Retrieves a user by their username.
     *
     * @param username the username
     * @return the requested user
     */
    @GetMapping("/username/{username}")
    public UserDto getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }


    /**
     * Retrieves all registered users.
     *
     * @return a list of users
     */
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * 🟢 Responsibility: Allows an administrator to securely block a user account by ID.
     * Accessible only by users holding the 'ADMIN' role.
     */
    @PutMapping("/admin/users/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> blockUser(@PathVariable Long id) {
        userService.blockUser(id);
        return ResponseEntity.ok("User blocked successfully.");
    }

    /**
     * 🟢 Responsibility: Allows an administrator to securely unblock/activate a user account by ID.
     * Accessible only by users holding the 'ADMIN' role.
     */
    @PutMapping("/admin/users/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> unblockUser(@PathVariable Long id) {
        userService.unblockUser(id);
        return ResponseEntity.ok("User activated successfully.");
    }
}