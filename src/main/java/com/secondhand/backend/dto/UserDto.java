package com.secondhand.backend.dto;

import com.secondhand.backend.entity.Role;
//import com.secondhand.backend.entity.;
import lombok.*;

@Data
@Builder
@Setter
@Getter

@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    private String fullName;

    private String username;

    private String email;

    private String phone;

    private Role role;

    private boolean isBlocked;
}