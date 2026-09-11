package com.fraudService.rules.advanced;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudSignal;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class AccountTakeoverRule implements AdvancedFraudRule {

    @Override
    public String getSignalId() {
        return "SIG_ACCOUNT_TAKEOVER";
    }

    @Override
    public String getSignalName() {
        return "Account Takeover (ATO) Pattern";
    }

    @Override
    public String getCategory() {
        return "ACCOUNT_SECURITY";
    }

    @Override
    public FraudSignal evaluateSignal(FraudCheckRequest request) {
        String deviceId = request.getDeviceId();
        BigDecimal amount = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;

        // Check if new/unrecognized device + high transaction amount (> ₹25,000)
        if (StringUtils.hasText(deviceId) && deviceId.toLowerCase().contains("new") && amount.compareTo(new BigDecimal("25000")) > 0) {
            return FraudSignal.flag(
                    getSignalId(),
                    getSignalName(),
                    getCategory(),
                    0.45,
                    0.85,
                    "High-value payment (" + amount + ") initiated immediately following unrecognized device switch (" + deviceId + ")",
                    Map.of("deviceId", deviceId, "amount", amount, "atoProbability", "HIGH")
            );
        }

        return FraudSignal.pass(getSignalId(), getSignalName(), getCategory());
    }
}
