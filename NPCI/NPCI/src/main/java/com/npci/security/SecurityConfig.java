package com.npci.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * SecurityConfig — NPCI service security configuration.
 *
 * This is an INTERNAL service — it should only be called by:
 * 1. API Gateway (authenticated requests)
 * 2. Other internal microservices
 *
 * Protection strategy:
 * - Validate X-Internal-Service-Key header (shared secret between services)
 * - Block all requests from unknown sources
 * - Actuator endpoints open for Kubernetes health checks
 *
 * JWT validation is done at Gateway level — not here.
 */

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${internal.service-key:internal-secret-change-in-prod}")
    private String internalServiceKey;

    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Actuator endpoints — open for K8s health probes
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // All NPCI endpoints require internal service header
                        .requestMatchers("/npci/**").authenticated()
                        .anyRequest().permitAll()
                )
                // Internal service key filter — validates X-Internal-Service-Key
                .addFilterBefore(internalServiceKeyFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    @Bean
    public InternalServiceKeyFilter internalServiceKeyFilter() {
        return new InternalServiceKeyFilter(internalServiceKey);
    }

    // ─── Inner Filter Class ───────────────────────────────────────────────────

    @Component
    @Slf4j
    public static class InternalServiceKeyFilter extends OncePerRequestFilter {

        private final String expectedKey;

        public InternalServiceKeyFilter(String expectedKey) {
            this.expectedKey = expectedKey;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain)
                throws ServletException, IOException {

            String path = request.getRequestURI();

            // Skip security for actuator paths
            if (path.startsWith("/actuator")) {
                filterChain.doFilter(request, response);
                return;
            }

            String serviceKey = request.getHeader("X-Internal-Service-Key");

            if (serviceKey == null || !serviceKey.equals(expectedKey)) {
                log.warn("🚫 Unauthorized request to NPCI service | path={} | ip={}",
                        path, request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Unauthorized — internal service key required\",\"errorCode\":\"UNAUTHORIZED\"}"
                );
                return;
            }

            // Log all incoming requests to NPCI service
            log.info("➡️  NPCI request | method={} | path={} | ip={}",
                    request.getMethod(), path, request.getRemoteAddr());

            filterChain.doFilter(request, response);
        }

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            // Don't apply filter to health check endpoints
            return request.getRequestURI().startsWith("/actuator");
        }
    }
}
