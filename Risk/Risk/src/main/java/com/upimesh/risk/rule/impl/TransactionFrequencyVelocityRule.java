package com.upimesh.risk.rule.impl;

import com.upimesh.risk.model.dto.VelocityResult;
import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.request.RiskScoringRequest;
import com.upimesh.risk.rule.RiskRule;
import com.upimesh.risk.rule.RiskRuleSeverity;
import com.upimesh.risk.rule.RuleEvaluationResult;
import com.upimesh.risk.service.RedisVelocityTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionFrequencyVelocityRule implements RiskRule {

    private final RedisVelocityTracker velocityTracker;

    @Override
    public String getRuleId() {
        return "RULE_TRANSACTION_VELOCITY";
    }

    @Override
    public String getRuleName() {
        return "Transaction Frequency & Velocity";
    }

    @Override
    public RuleEvaluationResult evaluate(RiskScoringRequest request, RiskProfile profile) {
        VelocityResult velRes = velocityTracker.trackAndEvaluateVelocity(
                request.getUserId(),
                request.getDeviceId(),
                request.getIpAddress(),
                request.getReceiverUpiId()
        );

        // Redis Degraded Fallback: Do NOT produce false clean result when Redis fails
        if (velRes.isDegraded()) {
            return RuleEvaluationResult.flag(
                    getRuleId(),
                    getRuleName(),
                    15.0,
                    RiskRuleSeverity.MEDIUM,
                    "Velocity tracker running in degraded mode due to Redis outage",
                    Map.of("degraded", true, "fallbackReason", velRes.getFallbackReason())
            );
        }

        // 1. Extreme 1m burst check (>10 txns/min for customer or device)
        if (velRes.getCustomerCount1m() > 10 || velRes.getDeviceCount1m() > 10 || velRes.getIpCount1m() > 15) {
            return RuleEvaluationResult.flag(
                    getRuleId(),
                    getRuleName(),
                    35.0,
                    RiskRuleSeverity.CRITICAL,
                    "Extreme velocity burst detected: " + velRes.getCustomerCount1m() + " txns/1m (cust), " + velRes.getDeviceCount1m() + " txns/1m (device)",
                    Map.of("cust_1m", velRes.getCustomerCount1m(), "dev_1m", velRes.getDeviceCount1m(), "ip_1m", velRes.getIpCount1m())
            );
        }

        // 2. High velocity check (>5 txns/1m or >15 txns/5m)
        if (velRes.getCustomerCount1m() > 5 || velRes.getCustomerCount5m() > 15) {
            return RuleEvaluationResult.flag(
                    getRuleId(),
                    getRuleName(),
                    20.0,
                    RiskRuleSeverity.HIGH,
                    "High customer velocity: " + velRes.getCustomerCount1m() + " txns/1m, " + velRes.getCustomerCount5m() + " txns/5m",
                    Map.of("cust_1m", velRes.getCustomerCount1m(), "cust_5m", velRes.getCustomerCount5m())
            );
        }

        // 3. Database profile timestamp check fallback
        if (profile != null && profile.getLastTransactionAt() != null) {
            Duration duration = Duration.between(profile.getLastTransactionAt(), LocalDateTime.now());
            long secondsSinceLast = Math.abs(duration.getSeconds());

            if (secondsSinceLast < 10) {
                return RuleEvaluationResult.flag(
                        getRuleId(),
                        getRuleName(),
                        15.0,
                        RiskRuleSeverity.MEDIUM,
                        "Consecutive transaction within " + secondsSinceLast + " seconds",
                        Map.of("secondsSinceLast", secondsSinceLast)
                );
            }
        }

        return RuleEvaluationResult.pass(getRuleId(), getRuleName());
    }
}
