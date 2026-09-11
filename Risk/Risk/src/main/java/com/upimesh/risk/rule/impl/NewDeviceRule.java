package com.upimesh.risk.rule.impl;

import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.request.RiskScoringRequest;
import com.upimesh.risk.rule.RiskRule;
import com.upimesh.risk.rule.RiskRuleSeverity;
import com.upimesh.risk.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class NewDeviceRule implements RiskRule {

    @Override
    public String getRuleId() {
        return "RULE_NEW_DEVICE";
    }

    @Override
    public String getRuleName() {
        return "New Device Recognition";
    }

    @Override
    public RuleEvaluationResult evaluate(RiskScoringRequest request, RiskProfile profile) {
        String deviceId = request.getDeviceId();
        if (!StringUtils.hasText(deviceId)) {
            return RuleEvaluationResult.pass(getRuleId(), getRuleName());
        }

        if (profile == null || profile.getKnownDevices() == null || profile.getKnownDevices().isEmpty()) {
            // First device for user
            return RuleEvaluationResult.pass(getRuleId(), getRuleName());
        }

        if (!profile.getKnownDevices().contains(deviceId)) {
            return RuleEvaluationResult.flag(
                    getRuleId(),
                    getRuleName(),
                    25.0,
                    RiskRuleSeverity.MEDIUM,
                    "Transaction initiated from an unrecognized device ID: " + deviceId,
                    Map.of("deviceId", deviceId, "knownDeviceCount", profile.getKnownDevices().size())
            );
        }

        return RuleEvaluationResult.pass(getRuleId(), getRuleName());
    }
}
