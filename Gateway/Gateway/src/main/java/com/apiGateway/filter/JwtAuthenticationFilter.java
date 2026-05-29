package com.apiGateway.filter;

import com.apiGateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * ================================================================
 * JWT Authentication Global Filter
 * ================================================================
 * API Gateway mein GlobalFilter implement kiya hai.
 * Yeh REACTIVE filter hai (WebFlux) - blocking nahi hai.
 *
 * Flow:
 * 1. Request aata hai
 * 2. Public endpoints? -> Skip filter, aage bhejo
 * 3. Authorization header check karo
 * 4. JWT token extract karo aur validate karo
 * 5. Valid hai to user info header mein add karo downstream services ke liye
 * 6. Invalid hai to 401 Unauthorized return karo
 *
 * Downstream services ko yeh headers milenge:
 * - X-User-Id: JWT se nikala userId
 * - X-User-Email: JWT se nikala email
 * - X-User-Roles: JWT se nikale roles
 * ================================================================
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {


    private final JwtUtil jwtUtil;

    // Yeh endpoints JWT ke bina access ho sakte hain
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/auth/register",
            "/auth/login",
            "/auth/verify-otp",
            "/auth/refresh-token",
            "/actuator/health",
            "/v3/api-docs",
            "/swagger-ui"
    );


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Public endpoint hai? Filter skip karo
        if(isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        // Authorization header check karo
        if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
            return onError(exchange, "Authorization header missing",  HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return onError(exchange, "Invalid authorization format. Use: Bearer <token>",
                    HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try{
            // JWT validate karo
            if(!jwtUtil.isTokenValid(token)){
                return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
            }

            // Token se user info nikalo
            String userId  = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);
            String roles = jwtUtil.extractRoles(token);

            // Downstream services ke liye headers add karo
            // Ab kisi bhi service ko token validate karne ki zarurat nahi
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Email", email)
                    .header("X-User-Roles", roles)
                    .build();

            log.debug("Authenticated request: userId={}, path={}", userId, path);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("JWT processing error: {}", e.getMessage());
            return onError(exchange, "Token processing failed", HttpStatus.UNAUTHORIZED);
        }

    }


    /**
     * Error response return karo - Reactive way
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format(
                "{\"error\":\"%s\",\"status\":%d,\"path\":\"%s\"}",
                message,
                status.value(),
                exchange.getRequest().getPath().value()
        );

        var buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }


    /**
     * Public endpoint check - starts with any public path
     */
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
