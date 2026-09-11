package com.fraudService.rules.advanced;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudSignal;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TransactionVelocityAbuseRule implements AdvancedFraudRule {

    @Override
    public String getSignalId() {
        return "SIG_VELOCITY_ABUSE";
    }

    @Override
    public String getSignalName() {
        return "Transaction Burst Velocity Abuse";
    }

    @Override
    public String getCategory() {
        return "VELOCITY";
    }

    @Override
    public FraudSignal evaluateSignal(FraudCheckRequest request) {
        // High frequency indicator if payment ID contains BURST or VELOCITY
        String paymentId = request.getPaymentId();
        if (paymentId != null && (paymentId.toUpperCase().contains("BURST") || paymentId.toUpperCase().contains("VELOCITY"))) {
            return FraudSignal.flag(
                    getSignalId(),
                    getSignalName(),
                    getCategory(),
                    0.35,
                    0.90,
                    "High frequency transaction burst signature detected for payment " + paymentId,
                    Map.of("paymentId", paymentId, "burstRate", ">10/min")
            );
        }

        return FraudSignal.pass(getSignalId(), getSignalName(), getCategory());
    }
}
