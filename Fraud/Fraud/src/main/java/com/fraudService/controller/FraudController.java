package com.fraudService.controller;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudCheckResponse;
import com.fraudService.entity.FraudLog;
import com.fraudService.repository.FraudLogRepository;
import com.fraudService.service.FraudDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ================================================================
 * Fraud Controller - REST Endpoints
 * ================================================================
 * POST /fraud/check -> Manual fraud check (payment-service se Feign call)
 * GET  /fraud/logs/{paymentId} -> Payment fraud log
 * GET  /fraud/history/{senderId} -> User fraud history
 * GET  /fraud/high-risk -> High risk payments (admin)
 * ================================================================
 */
@RestController
@RequestMapping("/fraud")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fraud Detection", description = "AI-powered fraud detection APIs")
public class FraudController {

    private final FraudDetectionService fraudDetectionService;
    private final FraudLogRepository fraudLogRepository;

    /**
     * POST /fraud/check
     * Manual fraud check (Kafka ke alawa direct call ke liye)
     */
    @PostMapping("/check")
    @Operation(
            summary = "Check payment for fraud",
            description = "Run fraud detection rules + AI scoring. Returns SAFE/ REVIEW / BLOCKED."
    )
    public ResponseEntity<FraudCheckResponse> checkFraud(
            @Valid @RequestBody FraudCheckRequest request){
        log.info("Manual fraud check request for paymentId={}", request.getPaymentId());
        FraudCheckResponse response = fraudDetectionService.checkFraud(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /fraud/logs/{paymentId}
     * Ek payment ka fraud check log
     */
    @GetMapping("/logs/{paymentId}")
    @Operation(summary = "Get fraud log for a payment")
    public ResponseEntity<FraudLog> getFraudLog(@PathVariable String paymentId){
        return fraudLogRepository.findByPaymentId(paymentId)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /fraud/history/{senderId}
     * User ka fraud history
     */
    @GetMapping("/history/{senderId}")
    @Operation(summary = "Get fraud history for a sender")
    public ResponseEntity<List<FraudLog>> getFraudHistory(@PathVariable Long senderId){
        List<FraudLog> history = fraudLogRepository.findBySenderIdOrderByCheckedAtDesc(senderId);
        return ResponseEntity.ok(history);
    }

    /**
     * GET /fraud/high-risk?minScore=0.7&since=hours
     * High risk payments (admin monitoring)
     */
    @GetMapping("/high-risk")
    @Operation(summary = "Get high-risk payments (admin - only )")
    public ResponseEntity<List<FraudLog>> getHighRiskPayments(
            @RequestParam(defaultValue = "0.7") Double minScore,
            @RequestParam(defaultValue = "24") int sinceHours) {

        LocalDateTime since = LocalDateTime.now().minusHours(sinceHours);
        List<FraudLog> highRisk = fraudLogRepository.findHighRiskPayments(minScore, since);
        return ResponseEntity.ok(highRisk);
    }

    /**
     * GET /fraud/stats
     * Fraud statistics summary
     */
    @GetMapping("/stats")
    @Operation(summary = "Fraud detection statistics")
    public ResponseEntity<Map<String, Object>> getStats() {
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        List<FraudLog> blocked = fraudLogRepository.findByFinalDecisionAndCheckedAtAfter("BLOCKED", last24h);
        List<FraudLog> review = fraudLogRepository.findByFinalDecisionAndCheckedAtAfter("REVIEW", last24h);
        List<FraudLog> safe = fraudLogRepository.findByFinalDecisionAndCheckedAtAfter("SAFE", last24h);

        return ResponseEntity.ok(Map.of(
                "last24Hours", Map.of(
                        "blocked", blocked.size(),
                        "review", review.size(),
                        "safe", safe.size(),
                        "total", blocked.size() + review.size() + safe.size()
                ),
                "generatedAt", LocalDateTime.now().toString()
        ));
    }

}
