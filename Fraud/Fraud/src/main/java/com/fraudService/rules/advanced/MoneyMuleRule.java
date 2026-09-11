package com.fraudService.rules.advanced;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudSignal;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class MoneyMuleRule implements AdvancedFraudRule {

    @Override
    public String getSignalId() {
        return "SIG_MONEY_MULE";
    }

    @Override
    public String getSignalName() {
        return "Money Mule Pass-Through Flow";
    }

    @Override
    public String getCategory() {
        return "MULE_BEHAVIOR";
    }

    @Override
    public FraudSignal evaluateSignal(FraudCheckRequest request) {
        String senderUpi = request.getSenderUpiId();
        String receiverUpi = request.getReceiverUpiId();
        BigDecimal amount = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;

        // Check if money mule pattern indicator in mode or UPI ID
        if (StringUtils.hasText(senderUpi) && StringUtils.hasText(receiverUpi)) {
            if (receiverUpi.toLowerCase().contains("mule") || receiverUpi.toLowerCase().contains("temp")) {
                return FraudSignal.flag(
                        getSignalId(),
                        getSignalName(),
                        getCategory(),
                        0.50,
                        0.90,
                        "Pass-through money mule transfer pattern detected targeting " + receiverUpi,
                        Map.of("senderUpi", senderUpi, "receiverUpi", receiverUpi, "amount", amount)
                );
            }
        }

        return FraudSignal.pass(getSignalId(), getSignalName(), getCategory());
    }
}
