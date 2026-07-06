package com.secondhand.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy; // 👈 رفع خطای اول
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 👈 رفع خطای سوم

@Configuration
@EnableWebSecurity
<<<<<<<< HEAD:backend/src/main/java/com/secondhand/backend/security/SecurityConfig.java
@RequiredArgsConstructor
public class SecurityConfig {

========
@RequiredArgsConstructor // 👈 این انوتیشن به لومبوک می‌گوید سازنده را برای فیلدهای final بسازد
public class SecurityConfig {

    // 👈 رفع خطای دوم: تعریف فیلد فیلتر به صورت final برای تزریق خودکار توسط لومبوک
>>>>>>>> 8c3c1ebddc797006ae2e9dd2cb7973bb0c3a1cac:src/main/java/com/secondhand/backend/security/SecurityConfig.java
    private final JwtAuthenticationFilter jwtAuthFilter;

    // ۱. برگشتن متد پسورد اینکودر برای حل ارور جدید اسپرینگ
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
<<<<<<<< HEAD:backend/src/main/java/com/secondhand/backend/security/SecurityConfig.java
                        // ۲. اضافه شدن مسیر فرانت‌اَند شما (/api/handle) به لیست مسیرهای کاملاً آزاد
                        .requestMatchers("/api/handle", "/api/users/register", "/api/users/login", "/h2-console/**").permitAll()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
========
                        // مسیرهای عمومی که نیاز به توکن ندارند (لاگین، رجیستر و مستندات سواگر)
                        .requestMatchers("/api/users/login", "/api/users/register").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // متدهای GET آگهی‌ها (تا همه بتوانند کالاها را ببینند و سرچ کنند)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/advertisements/**").permitAll()

                        // هر درخواست دیگری (مثل ساخت آگهی، چت کردن، بلاک کردن و...) نیاز به احراز هویت دارد
>>>>>>>> 8c3c1ebddc797006ae2e9dd2cb7973bb0c3a1cac:src/main/java/com/secondhand/backend/security/SecurityConfig.java
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

    @Bean
    public io.swagger.v3.oas.models.OpenAPI customOpenAPI() {
        return new io.swagger.v3.oas.models.OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("SecondHand Marketplace API")
                        .version("1.0")
                        .description("مستندات متدهای بک‌اند پروژه اپلیکیشن دست‌دوم"))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("BearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("BearerAuth",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .name("BearerAuth")
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}