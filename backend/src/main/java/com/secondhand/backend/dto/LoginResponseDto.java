package com.secondhand.backend.dto;


import com.secondhand.backend.entity.Role;
import lombok.*;

@Data
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private Long id;

    private String fullName;

    private String username;

    private Role role;

    private String token;
}