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

@RestController
@RequestMapping("/fraud")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fraud Detection", description = "Fraud Intelligence APIs")
public class FraudController {

    private final FraudDetectionService fraudDetectionService;
    private final FraudLogRepository fraudLogRepository;

    @PostMapping({"/check", "/internal/check"})
    @Operation(summary = "Check payment for fraud")
    public ResponseEntity<FraudCheckResponse> checkFraud(@Valid @RequestBody FraudCheckRequest request) {
        log.info("Fraud check request for paymentId={}", request.getPaymentId());
        FraudCheckResponse response = fraudDetectionService.checkFraud(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logs/{paymentId}")
    @Operation(summary = "Get fraud log for a payment")
    public ResponseEntity<FraudLog> getFraudLog(@PathVariable String paymentId) {
        return fraudLogRepository.findByPaymentId(paymentId)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/signals/{paymentId}")
    @Operation(summary = "Get detailed fraud signals and evidence map for a payment")
    public ResponseEntity<Map<String, Object>> getFraudSignals(@PathVariable String paymentId) {
        return fraudLogRepository.findByPaymentId(paymentId)
                .map(log -> ResponseEntity.ok(Map.<String, Object>of(
                        "paymentId", log.getPaymentId(),
                        "intelligenceDecision", log.getIntelligenceDecision() != null ? log.getIntelligenceDecision() : log.getFinalDecision(),
                        "riskLevel", log.getRiskLevel() != null ? log.getRiskLevel() : "UNKNOWN",
                        "riskScore", log.getRiskScore() != null ? log.getRiskScore() : 0.0,
                        "evidence", log.getEvidenceMap() != null ? log.getEvidenceMap() : Map.of(),
                        "reasons", log.getReasons() != null ? log.getReasons() : ""
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history/{senderId}")
    @Operation(summary = "Get fraud history for a sender")
    public ResponseEntity<List<FraudLog>> getFraudHistory(@PathVariable Long senderId) {
        List<FraudLog> history = fraudLogRepository.findBySenderIdOrderByCheckedAtDesc(senderId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/investigations")
    @Operation(summary = "Get pending fraud investigation queue")
    public ResponseEntity<List<FraudLog>> getPendingInvestigations() {
        List<FraudLog> pending = fraudLogRepository.findByInvestigationStatusOrderByCheckedAtDesc("PENDING");
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/investigate/{paymentId}")
    @Operation(summary = "Update fraud investigation status (CONFIRMED_FRAUD / DISMISSED_FALSE_POSITIVE)")
    public ResponseEntity<Map<String, Object>> updateInvestigation(
            @PathVariable String paymentId,
            @RequestParam String status) {

        return fraudLogRepository.findByPaymentId(paymentId)
                .map(log -> {
                    log.setInvestigationStatus(status.toUpperCase());
                    fraudLogRepository.save(log);
                    return ResponseEntity.ok(Map.<String, Object>of(
                            "paymentId", paymentId,
                            "investigationStatus", log.getInvestigationStatus(),
                            "updatedAt", LocalDateTime.now().toString()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/high-risk")
    @Operation(summary = "Get high-risk payments")
    public ResponseEntity<List<FraudLog>> getHighRiskPayments(
            @RequestParam(defaultValue = "0.7") Double minScore,
            @RequestParam(defaultValue = "24") int sinceHours) {

        LocalDateTime since = LocalDateTime.now().minusHours(sinceHours);
        List<FraudLog> highRisk = fraudLogRepository.findHighRiskPayments(minScore, since);
        return ResponseEntity.ok(highRisk);
    }

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
