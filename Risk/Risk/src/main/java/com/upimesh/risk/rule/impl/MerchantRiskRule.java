package com.upimesh.risk.rule.impl;

import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.request.RiskScoringRequest;
import com.upimesh.risk.rule.RiskRule;
import com.upimesh.risk.rule.RiskRuleSeverity;
import com.upimesh.risk.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
public class MerchantRiskRule implements RiskRule {

    private static final List<String> HIGH_RISK_KEYWORDS = List.of(
            "GAMBLE", "CASINO", "BET", "CRYPTO", "FOREX", "OFFSHORE", "RISK"
    );

    @Override
    public String getRuleId() {
        return "RULE_MERCHANT_RISK";
    }

    @Override
    public String getRuleName() {
        return "Merchant Category & Handle Risk";
    }

    @Override
    public RuleEvaluationResult evaluate(RiskScoringRequest request, RiskProfile profile) {
        String merchantCategory = request.getMerchantCategory();
        String receiverUpiId = request.getReceiverUpiId();

        String combined = ((merchantCategory != null ? merchantCategory : "") + " " + (receiverUpiId != null ? receiverUpiId : "")).toUpperCase();

        for (String keyword : HIGH_RISK_KEYWORDS) {
            if (combined.contains(keyword)) {
                return RuleEvaluationResult.flag(
                        getRuleId(),
                        getRuleName(),
                        30.0,
                        RiskRuleSeverity.HIGH,
                        "High-risk merchant category/keyword detected: " + keyword,
                        Map.of("keyword", keyword, "receiverUpiId", receiverUpiId != null ? receiverUpiId : "")
                );
            }
        }

        return RuleEvaluationResult.pass(getRuleId(), getRuleName());
    }
}
