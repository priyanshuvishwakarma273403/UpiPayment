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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String deviceId;
    private String ipAddress;

    // Final fraud decision: SAFE / REVIEW / BLOCKED
    private String finalDecision;

    // Fraud intelligence granular decision: LOW RISK / SUSPICIOUS / HIGH RISK / REQUIRES INVESTIGATION
    private String intelligenceDecision;
    private String riskLevel;

    // Composite risk score (0.0 - 1.0)
    private Double riskScore;

    // AI model ka score
    private Double aiScore;

    // Individual rule results
    private List<RuleResult> ruleResults;

    private String fraudSignalsJson;

    @Builder.Default
    private Map<String, Object> evidenceMap = new HashMap<>();

    // Investigation status: PENDING / CONFIRMED_FRAUD / DISMISSED_FALSE_POSITIVE
    @Indexed
    private String investigationStatus;

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
