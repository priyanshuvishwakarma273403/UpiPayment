package com.upimesh.risk.rule.impl;

import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.request.RiskScoringRequest;
import com.upimesh.risk.rule.RiskRule;
import com.upimesh.risk.rule.RiskRuleSeverity;
import com.upimesh.risk.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
public class AmountDeviationRule implements RiskRule {

    @Override
    public String getRuleId() {
        return "RULE_AMOUNT_DEVIATION";
    }

    @Override
    public String getRuleName() {
        return "Transaction Amount Deviation";
    }

    @Override
    public RuleEvaluationResult evaluate(RiskScoringRequest request, RiskProfile profile) {
        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return RuleEvaluationResult.pass(getRuleId(), getRuleName());
        }

        if (profile == null || profile.getAvgTransactionAmount() == null || profile.getAvgTransactionAmount().compareTo(BigDecimal.ZERO) <= 0) {
            // New user without historical average
            if (amount.compareTo(new BigDecimal("50000")) > 0) {
                return RuleEvaluationResult.flag(
                        getRuleId(),
                        getRuleName(),
                        25.0,
                        RiskRuleSeverity.MEDIUM,
                        "High initial transaction amount without historical profile",
                        Map.of("amount", amount, "historicalAvg", 0)
                );
            }
            return RuleEvaluationResult.pass(getRuleId(), getRuleName());
        }

        BigDecimal avg = profile.getAvgTransactionAmount();
        BigDecimal ratio = amount.divide(avg, 2, RoundingMode.HALF_UP);

        if (ratio.compareTo(new BigDecimal("10.0")) >= 0) {
            return RuleEvaluationResult.flag(
                    getRuleId(),
                    getRuleName(),
                    35.0,
                    RiskRuleSeverity.HIGH,
                    "Transaction amount is " + ratio + "x higher than historical average (" + avg + ")",
                    Map.of("amount", amount, "historicalAvg", avg, "ratio", ratio)
            );
        } else if (ratio.compareTo(new BigDecimal("5.0")) >= 0) {
            return RuleEvaluationResult.flag(
                    getRuleId(),
                    getRuleName(),
                    20.0,
                    RiskRuleSeverity.MEDIUM,
                    "Transaction amount is " + ratio + "x higher than historical average (" + avg + ")",
                    Map.of("amount", amount, "historicalAvg", avg, "ratio", ratio)
            );
        }

        return RuleEvaluationResult.pass(getRuleId(), getRuleName());
    }
}
