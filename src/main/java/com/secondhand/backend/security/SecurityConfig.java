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
@RequiredArgsConstructor // برای تزریق خودکار فیلتر
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter; // 👈 تزریق فیلتر جدید

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // ۱. مشخص کردن مسیرهای آزاد و مسیرهای نیازمند قفل امنیتی
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll() // ثبت نام و لاگین برای همه باز است
                        .requestMatchers("/api/users/**").hasRole("ADMIN") // بلاک و آنبلاک فقط برای ادمین است
                        .anyRequest().authenticated() // بقیه بخش‌ها (پیام‌ها، چت‌ها، امتیازها و آگهی‌ها) حتماً نیاز به توکن دارند
                )
                // ۲. مدیریت نشست‌ها به صورت Stateless (چون از توکن استفاده می‌کنیم نه Session)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // ۳. اضافه کردن فیلتر JWT دقیقاً قبل از فیلتر اصلی احراز هویت اسپرینگ
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}