package com.fraudService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ================================================================
 * FraudLog - MongoDB Document
 * ================================================================
 * Collection: fraud_logs
 *
 * Har payment ke fraud check ka complete record yahan store hoga.
 * MongoDB use karne ke reasons:
 * 1. Schema-flexible: Rules change hone par columns add karne ki
 *    zarurat nahi
 * 2. Rich queries: Fraud patterns dhundne ke liye complex queries
 * 3. Fast writes: High volume payment events
 * 4. TTL index: 90 din purane logs auto-delete ho sakte hain
 * ================================================================
 */


@Document(collection = "fraud_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudLog {

    @Id
    private String id;

    @Indexed
    private String paymentId;

    @Indexed
    private Long senderId;

    private Long receiverId;
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private String paymentMode;

    // Final fraud decision: SAFE / REVIEW / BLOCKED
    private String finalDecision;

    // Composite risk score (0.0 - 1.0)
    private Double riskScore;

    // AI model ka score
    private Double aiScore;

    // Individual rule results
    private List<RuleResult> ruleResults;

    // Human readable reasons
    private String reasons;

    // Action taken (ALLOWED / BLOCKED / FLAGGED_FOR_REVIEW)
    private String actionTaken;

    @Indexed
    private LocalDateTime checkedAt;

    // Processing time (milliseconds)
    private Long processingTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleResult {
        private String ruleName;
        private String decision;
        private String reason;
        private Double riskScore;
    }
}
