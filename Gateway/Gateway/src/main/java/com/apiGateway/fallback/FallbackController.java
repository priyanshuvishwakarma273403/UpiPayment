package com.apiGateway.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ================================================================
 * Fallback Controller - Circuit Breaker Fallback
 * ================================================================
 * Jab koi downstream service unavailable hoti hai (circuit open),
 * Gateway is controller ke endpoints par redirect karta hai.
 *
 * User ko graceful error message milta hai,
 * na ki raw connection refused exception.
 *
 * Application.yml mein:
 * fallbackUri: forward:/fallback/auth  -> yahan aata hai
 * ================================================================
 */

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    /** Auth Service fallback */
    @GetMapping("/auth")
    @RequestMapping("/fallback/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authFallback() {
        log.warn("Auth service circuit breaker triggered - returning fallback");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildFallbackResponse("auth-service",
                        "Authentication service is temporarily unavailable. Please try again in a moment.")));
    }

    /** Wallet Service fallback */
    @RequestMapping("/fallback/wallet")
    public Mono<ResponseEntity<Map<String, Object>>> walletFallback() {
        log.warn("Wallet service circuit breaker triggered - returning fallback");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildFallbackResponse("wallet-service",
                        "Wallet service is temporarily unavailable. Your balance is safe.")));
    }

    /** Payment Service fallback */
    @RequestMapping("/fallback/payment")
    public Mono<ResponseEntity<Map<String, Object>>> paymentFallback() {
        log.warn("Payment service circuit breaker triggered - returning fallback");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildFallbackResponse("payment-service",
                        "Payment service is temporarily unavailable. Please retry. No money has been deducted.")));
    }

    /** Generic fallback */
    @RequestMapping("/fallback/default")
    public Mono<ResponseEntity<Map<String, Object>>> defaultFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildFallbackResponse("service",
                        "Service is temporarily unavailable. Please try again later.")));
    }

    private Map<String, Object> buildFallbackResponse(String service, String message) {
        return Map.of(
                "success", false,
                "error", "SERVICE_UNAVAILABLE",
                "service", service,
                "message", message,
                "timestamp", LocalDateTime.now().toString(),
                "retryAfter", "30 seconds"
        );
    }

}
