package com.kyc_Service.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ================================================================
 * Security Configuration - KYC Service
 * ================================================================
 * KYC service ke andar JWT validate nahi hota.
 * API Gateway pehle validate karta hai aur
 * X-User-Id header inject karta hai.
 *
 * Stateless: Koi session nahi.
 * CSRF disabled: REST API ke liye zaruri nahi.
 * ================================================================
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/**",
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // Gateway already authenticated - trust X-User-Id header
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}
