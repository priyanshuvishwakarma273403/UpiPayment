package com.notification_service.contoller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ================================================================
 * Notification Controller
 * ================================================================
 * Notification service mainly Kafka-driven hai.
 * REST endpoints status aur testing ke liye hain.
 *
 * GET  /notifications/health  -> Service health
 * POST /notifications/test    -> Test notification bhejo (dev only)
 * ================================================================
 */
@RestController
@RequestMapping("/notifications")
@Slf4j
@Tag(name = "Notifications", description = "Notification service status and test APIs")
public class NotificationController {

    /**
     * GET /notifications/health
     * Service status check
     */
    @GetMapping("/health")
    @Operation(summary = "Notification service health status")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "service",   "notification-service",
                "status",    "UP",
                "timestamp", LocalDateTime.now().toString(),
                "consumers", Map.of(
                        "payment_completed", "ACTIVE",
                        "payment_failed",    "ACTIVE",
                        "sync_completed",    "ACTIVE"
                )
        ));
    }

    /**
     * GET /notifications/status
     * Notification stats
     */
    @GetMapping("/status")
    @Operation(summary = "Get notification service stats")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "service",      "notification-service",
                "emailEnabled", true,
                "smsEnabled",   false,
                "uptime",       "Running"
        ));
    }
}
