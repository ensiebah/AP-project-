package com.secondhand.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "my_super_secret_key_for_secondhand_application_project_2026";
    private static final long EXPIRATION_TIME = 86400000;

    // نام متد را به getSignKey تغییر دادیم تا خطای کامپایلر برطرف شود
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a JWT token for the specified user.
     *
     * @param username the authenticated user's username
     * @param role the user's role
     * @return a signed JWT token
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignKey()) // بدون خطا صدا زده می‌شود
                .compact();
    }


    /**
     * Extracts the username from the specified JWT token.
     *
     * @param token the JWT token
     * @return the username stored in the token
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts the user's role from the specified JWT token.
     *
     * @param token the JWT token
     * @return the role stored in the token
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Checks whether the specified JWT token has expired.
     *
     * @param token the JWT token
     * @return {@code true} if the token is expired; otherwise {@code false}
     */
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Validates the specified JWT token against the provided username.
     *
     * @param token the JWT token
     * @param username the expected username
     * @return {@code true} if the token is valid; otherwise {@code false}
     */
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * Extracts all claims contained in the specified JWT token.
     *
     * @param token the JWT token
     * @return the claims stored in the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey()) // بدون خطا صدا زده می‌شود
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}