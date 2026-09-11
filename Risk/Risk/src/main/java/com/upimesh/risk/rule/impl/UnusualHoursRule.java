package com.upimesh.risk.rule.impl;

import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.request.RiskScoringRequest;
import com.upimesh.risk.rule.RiskRule;
import com.upimesh.risk.rule.RiskRuleSeverity;
import com.upimesh.risk.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class UnusualHoursRule implements RiskRule {

    @Override
    public String getRuleId() {
        return "RULE_UNUSUAL_HOURS";
    }

    @Override
    public String getRuleName() {
        return "Unusual Operating Hours";
    }

    @Override
    public RuleEvaluationResult evaluate(RiskScoringRequest request, RiskProfile profile) {
        int currentHour = LocalDateTime.now().getHour();

        // High risk hours: 1 AM to 5 AM
        if (currentHour >= 1 && currentHour <= 5) {
            return RuleEvaluationResult.flag(
                    getRuleId(),
                    getRuleName(),
                    20.0,
                    RiskRuleSeverity.MEDIUM,
                    "Transaction initiated during high-risk night hours (" + currentHour + ":00)",
                    Map.of("currentHour", currentHour)
            );
        }

        if (profile != null && profile.getTypicalTransactionHours() != null && !profile.getTypicalTransactionHours().isEmpty()) {
            if (!profile.getTypicalTransactionHours().contains(currentHour)) {
                return RuleEvaluationResult.flag(
                        getRuleId(),
                        getRuleName(),
                        10.0,
                        RiskRuleSeverity.LOW,
                        "Transaction hour (" + currentHour + ":00) outside customer habitual profile",
                        Map.of("currentHour", currentHour, "typicalHours", profile.getTypicalTransactionHours())
                );
            }
        }

        return RuleEvaluationResult.pass(getRuleId(), getRuleName());
    }
}
