package com.upimesh.risk.rule.impl;

import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.request.RiskScoringRequest;
import com.upimesh.risk.rule.RiskRule;
import com.upimesh.risk.rule.RiskRuleSeverity;
import com.upimesh.risk.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class AccountAgeRule implements RiskRule {

    @Override
    public String getRuleId() {
        return "RULE_ACCOUNT_AGE";
    }

    @Override
    public String getRuleName() {
        return "Account Age Risk";
    }

    @Override
    public RuleEvaluationResult evaluate(RiskScoringRequest request, RiskProfile profile) {
        if (profile == null || profile.getCreatedAt() == null) {
            return RuleEvaluationResult.pass(getRuleId(), getRuleName());
        }

        long ageDays = Duration.between(profile.getCreatedAt(), LocalDateTime.now()).toDays();

        if (ageDays < 7) {
            return RuleEvaluationResult.flag(
                    getRuleId(),
                    getRuleName(),
                    25.0,
                    RiskRuleSeverity.HIGH,
                    "New account created only " + ageDays + " days ago",
                    Map.of("accountAgeDays", ageDays)
            );
        } else if (ageDays < 30) {
            return RuleEvaluationResult.flag(
                    getRuleId(),
                    getRuleName(),
                    10.0,
                    RiskRuleSeverity.LOW,
                    "Recent account created " + ageDays + " days ago",
                    Map.of("accountAgeDays", ageDays)
            );
        }

        return RuleEvaluationResult.pass(getRuleId(), getRuleName());
    }
}
