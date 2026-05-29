package com.apiGateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ================================================================
 * Global Exception Handler - WebFlux (Reactive) Version
 * ================================================================
 * API Gateway WebFlux use karta hai, isliye normal
// * @ControllerAdvice kaam nahi karta.
 *
 * ErrorWebExceptionHandler implement karo - yeh reactive error handler hai.
// * @Order(-1): Spring ke default error handler se pehle run hoga.
 *
 * Saari unhandled exceptions yahan catch hongi aur
 * proper JSON response return hoga.
 * ================================================================
 */

@Component
@Order(-1)
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String message;
        String errorCode;

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason() != null ? rse.getReason() : rse.getMessage();
            errorCode = "HTTP_" + status.value();
        } else if (ex instanceof io.jsonwebtoken.ExpiredJwtException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "JWT token has expired. Please login again.";
            errorCode = "TOKEN_EXPIRED";
        } else if (ex instanceof io.jsonwebtoken.MalformedJwtException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Invalid JWT token format.";
            errorCode = "TOKEN_INVALID";
        } else if (ex instanceof java.net.ConnectException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service temporarily unavailable. Please retry.";
            errorCode = "SERVICE_UNAVAILABLE";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An unexpected error occurred.";
            errorCode = "INTERNAL_ERROR";
            log.error("Unhandled gateway exception: {}", ex.getMessage(), ex);
        }

        log.warn("Gateway error [{}]: {} | Path: {}",
                errorCode, message, exchange.getRequest().getPath().value());

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("error", errorCode);
        body.put("message", message);
        body.put("path", exchange.getRequest().getPath().value());
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Error writing error response: {}", e.getMessage());
            return Mono.error(e);
        }
    }
}
