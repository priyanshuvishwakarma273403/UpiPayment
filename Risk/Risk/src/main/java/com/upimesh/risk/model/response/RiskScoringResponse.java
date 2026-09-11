package com.upimesh.risk.model.response;

import com.upimesh.risk.model.enums.RiskDecision;
import com.upimesh.risk.model.enums.RiskLevel;
import com.upimesh.risk.rule.RuleEvaluationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskScoringResponse {
    private String transactionId;
    private String scoringId;
    private double finalScore;
    private RiskLevel riskLevel;
    private RiskDecision decision;
    private String actionRecommended;
    private boolean allowed;
    private List<String> factorsTriggered;
    private List<RuleEvaluationResult> ruleDetails;
    private String ruleVersion;
    private LocalDateTime scoredAt;
}
