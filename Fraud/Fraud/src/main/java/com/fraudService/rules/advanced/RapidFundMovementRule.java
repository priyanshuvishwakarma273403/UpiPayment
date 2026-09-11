package com.fraudService.rules.advanced;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudSignal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class RapidFundMovementRule implements AdvancedFraudRule {

    @Override
    public String getSignalId() {
        return "SIG_RAPID_FUND_MOVEMENT";
    }

    @Override
    public String getSignalName() {
        return "Rapid Fund Movement";
    }

    @Override
    public String getCategory() {
        return "FUND_VELOCITY";
    }

    @Override
    public FraudSignal evaluateSignal(FraudCheckRequest request) {
        BigDecimal amount = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;
        String mode = request.getPaymentMode();

        if ("OFFLINE".equalsIgnoreCase(mode) && amount.compareTo(new BigDecimal("100000")) > 0) {
            return FraudSignal.flag(
                    getSignalId(),
                    getSignalName(),
                    getCategory(),
                    0.35,
                    0.80,
                    "High-value offline rapid fund movement signature (Amount: ₹" + amount + ")",
                    Map.of("amount", amount, "mode", mode)
            );
        }

        return FraudSignal.pass(getSignalId(), getSignalName(), getCategory());
    }
}
