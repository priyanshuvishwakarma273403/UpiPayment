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
public class LocationDeviationRule implements RiskRule {

    @Override
    public String getRuleId() {
        return "RULE_LOCATION_DEVIATION";
    }

    @Override
    public String getRuleName() {
        return "Geographic Location Deviation";
    }

    @Override
    public RuleEvaluationResult evaluate(RiskScoringRequest request, RiskProfile profile) {
        String city = request.getCity();
        if (!StringUtils.hasText(city)) {
            city = request.getLocation();
        }

        if (!StringUtils.hasText(city)) {
            return RuleEvaluationResult.pass(getRuleId(), getRuleName());
        }

        if (profile == null || profile.getUsualCities() == null || profile.getUsualCities().isEmpty()) {
            return RuleEvaluationResult.pass(getRuleId(), getRuleName());
        }

        if (!profile.getUsualCities().contains(city)) {
            return RuleEvaluationResult.flag(
                    getRuleId(),
                    getRuleName(),
                    20.0,
                    RiskRuleSeverity.MEDIUM,
                    "Transaction city '" + city + "' differs from customer habitual location profile",
                    Map.of("city", city, "usualCities", profile.getUsualCities())
            );
        }

        return RuleEvaluationResult.pass(getRuleId(), getRuleName());
    }
}
