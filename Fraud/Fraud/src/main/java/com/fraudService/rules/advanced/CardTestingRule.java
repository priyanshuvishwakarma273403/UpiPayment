package com.fraudService.rules.advanced;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudSignal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class CardTestingRule implements AdvancedFraudRule {

    @Override
    public String getSignalId() {
        return "SIG_CARD_TESTING";
    }

    @Override
    public String getSignalName() {
        return "Card & Account Validation Testing";
    }

    @Override
    public String getCategory() {
        return "TESTING_PATTERN";
    }

    @Override
    public FraudSignal evaluateSignal(FraudCheckRequest request) {
        BigDecimal amount = request.getAmount();
        if (amount == null) {
            return FraudSignal.pass(getSignalId(), getSignalName(), getCategory());
        }

        // Micro amounts between ₹1.00 and ₹10.00
        if (amount.compareTo(new BigDecimal("1.00")) >= 0 && amount.compareTo(new BigDecimal("10.00")) <= 0) {
            return FraudSignal.flag(
                    getSignalId(),
                    getSignalName(),
                    getCategory(),
                    0.25,
                    0.75,
                    "Micro-transaction testing signature detected (Amount: ₹" + amount + ")",
                    Map.of("amount", amount, "pattern", "MICRO_AMOUNT_TESTING")
            );
        }

        return FraudSignal.pass(getSignalId(), getSignalName(), getCategory());
    }
}
