package com.fraudService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckResponse {

    private String paymentId;

    /** Legacy fraud decision: SAFE / REVIEW / BLOCKED */
    private FraudDecision decision;

    /** Granular decision: LOW RISK / SUSPICIOUS / HIGH RISK / REQUIRES INVESTIGATION */
    private String intelligenceDecision;

    private String riskLevel;

    private Boolean allowed;

    /** Composite risk score: 0.0 (safe) to 1.0 (high risk) */
    private Double riskScore;

    /** AI model score */
    private Double aiScore;

    /** Human readable reasons (for audit) */
    private String reasons;

    /** Individual rule details */
    private List<RuleDetail> ruleDetails;

    private LocalDateTime checkedAt;

    /** Processing time in ms */
    private Long processingTimeMs;

    public enum FraudDecision {
        SAFE,    // Payment allow karo
        REVIEW,  // Manual review ke liye flag karo
        BLOCKED  // Payment block karo
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleDetail {
        private String ruleName;
        private String decision;
        private String reason;
        private Double score;
    }
}
