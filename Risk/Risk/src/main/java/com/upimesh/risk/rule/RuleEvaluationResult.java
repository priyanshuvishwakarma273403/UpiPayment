package com.upimesh.risk.rule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Standardized result returned by every RiskRule evaluation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEvaluationResult {

    private String ruleId;
    private String ruleName;
    private double scoreContribution; // 0.0 to 100.0 (e.g. 15.0)
    private RiskRuleSeverity severity;
    private boolean triggered;
    private String reason;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    public static RuleEvaluationResult pass(String ruleId, String ruleName) {
        return RuleEvaluationResult.builder()
                .ruleId(ruleId)
                .ruleName(ruleName)
                .scoreContribution(0.0)
                .severity(RiskRuleSeverity.LOW)
                .triggered(false)
                .reason("Rule passed clean")
                .build();
    }

    public static RuleEvaluationResult flag(String ruleId, String ruleName, double score, RiskRuleSeverity severity, String reason, Map<String, Object> metadata) {
        return RuleEvaluationResult.builder()
                .ruleId(ruleId)
                .ruleName(ruleName)
                .scoreContribution(score)
                .severity(severity)
                .triggered(true)
                .reason(reason)
                .metadata(metadata != null ? metadata : new HashMap<>())
                .build();
    }
}
