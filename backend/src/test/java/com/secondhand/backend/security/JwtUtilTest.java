package com.secondhand.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String token;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        token = jwtUtil.generateToken("ali", "USER");
    }

    @Test
    void generateToken_ShouldReturnToken() {

        assertNotNull(token);
        assertFalse(token.isEmpty());

    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {

        String username = jwtUtil.extractUsername(token);

        assertEquals("ali", username);

    }

    @Test
    void extractRole_ShouldReturnCorrectRole() {

        String role = jwtUtil.extractRole(token);

        assertEquals("USER", role);

    }

    @Test
    void validateToken_ShouldReturnTrue_WhenUsernameMatches() {

        assertTrue(jwtUtil.validateToken(token, "ali"));

    }

    @Test
    void validateToken_ShouldReturnFalse_WhenUsernameDoesNotMatch() {

        assertFalse(jwtUtil.validateToken(token, "reza"));

    }

    @Test
    void tokenShouldNotBeExpiredImmediately() {

        assertFalse(jwtUtil.isTokenExpired(token));

    }

}