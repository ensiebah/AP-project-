package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.LoginRequestDto;
import com.secondhand.backend.dto.LoginResponseDto;
import com.secondhand.backend.dto.RegisterRequestDto;
import com.secondhand.backend.dto.UserDto;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.DuplicateEmailException;
import com.secondhand.backend.exception.DuplicateUsernameException;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.UseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UseService {
    private final UserRepository userRepository ;
    private final UserMapper userMapper ;

    @Override
    public UserDto register(RegisterRequestDto request){
        if (userRepository.existsByUserName(request.getUsername())){
            throw new DuplicateUsernameException("username already exists" );

        }
        if (userRepository.existsByEmail(request.getEmail())){
            throw new DuplicateEmailException("Email already exists") ;
        }
        User user = userMapper.toEntity(request) ;
        User savedUser = userRepository.save(user) ;
        return userMapper.toDto(savedUser) ;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request){
        User user = userRepository.findByUserName()
    }
}
