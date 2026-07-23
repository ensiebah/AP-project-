package com.secondhand.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Creates the password encoder bean used to hash and verify user passwords.
     *
     * @return the configured password encoder
     */
    // ۱. برگشتن متد پسورد اینکودر برای حل ارور جدید اسپرینگ
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * Configures the application's security rules, authentication filter,
     * session management, and endpoint access permissions.
     *
     * @param http the HttpSecurity configuration object
     * @return the configured security filter chain
     * @throws Exception if a security configuration error occurs
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // ۲. اضافه شدن مسیر فرانت‌اَند شما (/api/handle) به لیست مسیرهای کاملاً آزاد
                        .requestMatchers(
                                "/api/handle",
                                "/api/users/register",
                                "/api/users/login",
                                "/api/lookup/**",
                                "/api/images/file/**",
                                "/h2-console/**"
                        ).permitAll()
                        .requestMatchers("/api/admin/**", "/api/users/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // باز کردن دسترسی H2-Console
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}