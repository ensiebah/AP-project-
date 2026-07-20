package com.secondhand.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    private String Id ;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 4 , max = 20 , message = "username must be between 4 and 20 character")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
    message = "username can contain only letters , numbers , and underscore")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 4 , message = "password size must be at least 4 character")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^09\\d{9}$",
            message = "phone number must be valid"
    )
    private String phone;
}
