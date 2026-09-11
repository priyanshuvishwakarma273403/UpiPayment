package com.upimesh.risk.rule.impl;

import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.request.RiskScoringRequest;
import com.upimesh.risk.rule.RiskRule;
import com.upimesh.risk.rule.RiskRuleSeverity;
import com.upimesh.risk.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PreviousFraudHistoryRule implements RiskRule {

    @Override
    public String getRuleId() {
        return "RULE_PREVIOUS_FRAUD_HISTORY";
    }

    @Override
    public String getRuleName() {
        return "Historical Fraud & Failure Rate";
    }

    @Override
    public RuleEvaluationResult evaluate(RiskScoringRequest request, RiskProfile profile) {
        if (profile == null) {
            return RuleEvaluationResult.pass(getRuleId(), getRuleName());
        }

        if (profile.getBaseRiskScore() >= 0.7) {
            return RuleEvaluationResult.flag(
                    getRuleId(),
                    getRuleName(),
                    40.0,
                    RiskRuleSeverity.CRITICAL,
                    "High customer base risk score from previous fraud/flagged activity: " + profile.getBaseRiskScore(),
                    Map.of("baseRiskScore", profile.getBaseRiskScore())
            );
        } else if (profile.getFailedTransactions() > 5 && profile.getSuccessfulTransactions() == 0) {
            return RuleEvaluationResult.flag(
                    getRuleId(),
                    getRuleName(),
                    35.0,
                    RiskRuleSeverity.HIGH,
                    "Multiple failed transactions without successful history",
                    Map.of("failedTransactions", profile.getFailedTransactions())
            );
        }

        return RuleEvaluationResult.pass(getRuleId(), getRuleName());
    }
}
