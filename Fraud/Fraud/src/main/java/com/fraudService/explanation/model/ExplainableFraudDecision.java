package com.fraudService.explanation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplainableFraudDecision {

    private String paymentId;
    private String customerId;
    private String overallDecision; // LOW RISK, SUSPICIOUS, HIGH RISK, REQUIRES INVESTIGATION
    private double overallRiskScore; // 0.0 - 1.0
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private String summaryQuestion; // "Why was transaction PAY1001 considered risky?"

    private List<RuleEvidenceItem> ruleEvidence;
    private Map<String, Object> behavioralEvidence;
    private MlEvidenceItem mlEvidence;
    private NetworkEvidenceItem networkEvidence;

    private String controlledDecisionBoundary;
    private LocalDateTime decisionTimestamp;
}
