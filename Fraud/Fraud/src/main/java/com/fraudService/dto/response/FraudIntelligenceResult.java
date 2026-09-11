package com.fraudService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Composite Fraud Intelligence Output.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudIntelligenceResult {

    private String paymentId;
    private double riskScore; // 0.0 to 1.0
    private FraudRiskLevel riskLevel; // LOW_RISK, SUSPICIOUS, HIGH_RISK, REQUIRES_INVESTIGATION
    private String decision; // LOW RISK, SUSPICIOUS, HIGH RISK, REQUIRES INVESTIGATION
    private String legacyDecision; // SAFE / REVIEW / BLOCKED
    private boolean allowed;
    private String explanation;
    private List<FraudSignal> fraudSignals;

    @Builder.Default
    private Map<String, Object> evidence = new HashMap<>();

    private LocalDateTime evaluatedAt;
    private long processingTimeMs;
}
