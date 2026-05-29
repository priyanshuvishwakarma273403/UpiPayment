package com.paymentService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ================================================================
 * Payment Service Security Config
 * ================================================================
 * Payment service ke andar JWT directly validate nahi hota.
 * API Gateway pehle validate karta hai aur phir
 * X-User-Id, X-User-Email, X-User-Roles headers inject karta hai.
 *
 * Isliye yahan:
 * - CSRF disabled (REST API)
 * - Stateless session
 * - Actuator aur Swagger public
 * - Baaki sab authenticated (header se user info lete hain)
 *
 * Production mein: Internal service-to-service communication
 * ke liye mTLS ya service mesh (Istio) use karo.
 * ================================================================
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement( s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll().anyRequest().authenticated()  // Gateway already authenticate kar chuka hai
                );
        return http.build();
    }


}
