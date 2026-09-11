package com.fraudService.rules.advanced;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudSignal;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class IdentityAnomalyRule implements AdvancedFraudRule {

    @Override
    public String getSignalId() {
        return "SIG_IDENTITY_ANOMALY";
    }

    @Override
    public String getSignalName() {
        return "Identity & Handle Mismatch Anomaly";
    }

    @Override
    public String getCategory() {
        return "IDENTITY_INTEGRITY";
    }

    @Override
    public FraudSignal evaluateSignal(FraudCheckRequest request) {
        String senderUpi = request.getSenderUpiId();
        Long senderId = request.getSenderId();

        if (StringUtils.hasText(senderUpi) && senderId != null) {
            // Check for invalid format or synthetic handle mismatch
            if (senderUpi.contains("anonymous") || senderUpi.contains("fake")) {
                return FraudSignal.flag(
                        getSignalId(),
                        getSignalName(),
                        getCategory(),
                        0.30,
                        0.85,
                        "Identity profile mismatch for sender handle: " + senderUpi,
                        Map.of("senderId", senderId, "senderUpiId", senderUpi)
                );
            }
        }

        return FraudSignal.pass(getSignalId(), getSignalName(), getCategory());
    }
}
