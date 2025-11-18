package com.example.seller_helper.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/index.html", "/signup.html", "/login.html",
                            "/auth/**", "/css/**", "/js/**", "/images/**").permitAll()
                    .anyRequest().permitAll()   // ★ 일단 전부 오픈 (JWT 붙이기 전까지)
            )
            .formLogin(form -> form.disable())   // ★ formLogin 비활성화
            .httpBasic(httpBasic -> httpBasic.disable()); // ★ 팝업 발생 원인 제거

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
