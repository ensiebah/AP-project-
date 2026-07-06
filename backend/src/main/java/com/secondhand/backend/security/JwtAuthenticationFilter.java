package com.secondhand.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // اگر درخواست به مسیر فرانت‌اَند یا ثبت‌نام و لاگین آمد، فیلتر اجرا نشود
        return path.equals("/api/handle")
                || path.equals("/api/users/login")
                || path.equals("/api/users/register")
                || path.startsWith("/h2-console");
    }
    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ۱. استخراج هدر Authorization از درخواست ورودی
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // ۲. اگر هدر خالی بود یا با Bearer شروع نمی‌شد، درخواست را بدون احراز هویت به فیلتر بعدی بفرست
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ۳. جدا کردن توکن اصلی از کلمه Bearer
        jwt = authHeader.substring(7);
        try {
            username = jwtUtil.extractUsername(jwt);

            // ۴. اگر یوزرنیم استخراج شد و کاربر هنوز در سناریوی جاری احراز هویت نشده بود
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // بررسی اعتبار زمانی و ساختاری توکن
                if (jwtUtil.validateToken(jwt, username)) {
                    String role = jwtUtil.extractRole(jwt);

                    // ساخت شیء احراز هویت برای اسپرینگ سکیوریتی همراه با نقش کاربر
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // قرار دادن کاربر در کانکست امنیتی (اسپرینگ حالا کاربر را می‌شناسد)
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // اگر توکن منقضی یا دستکاری شده بود، خطایی صادر نمی‌کنیم تا فیلتر سکیوریتی خودش آن را هندل کند
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }
}