package com.upimesh.risk.rule.impl;

import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.request.RiskScoringRequest;
import com.upimesh.risk.rule.RiskRule;
import com.upimesh.risk.rule.RiskRuleSeverity;
import com.upimesh.risk.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class NewBeneficiaryRule implements RiskRule {

    @Override
    public String getRuleId() {
        return "RULE_NEW_BENEFICIARY";
    }

    @Override
    public String getRuleName() {
        return "First Time Beneficiary";
    }

    @Override
    public RuleEvaluationResult evaluate(RiskScoringRequest request, RiskProfile profile) {
        String receiverUpiId = request.getReceiverUpiId();
        BigDecimal amount = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;

        if (!StringUtils.hasText(receiverUpiId)) {
            return RuleEvaluationResult.pass(getRuleId(), getRuleName());
        }

        // Check if high-value transfer to new beneficiary
        if (profile == null || profile.getTotalTransactions() < 3) {
            if (amount.compareTo(new BigDecimal("10000")) > 0) {
                return RuleEvaluationResult.flag(
                        getRuleId(),
                        getRuleName(),
                        15.0,
                        RiskRuleSeverity.LOW,
                        "High-value transfer to beneficiary with low transaction history",
                        Map.of("receiverUpiId", receiverUpiId, "amount", amount)
                );
            }
        }

        return RuleEvaluationResult.pass(getRuleId(), getRuleName());
    }
}
