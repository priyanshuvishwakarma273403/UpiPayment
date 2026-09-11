package com.fraudService.rules.advanced;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudSignal;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class BeneficiaryAbuseRule implements AdvancedFraudRule {

    @Override
    public String getSignalId() {
        return "SIG_BENEFICIARY_ABUSE";
    }

    @Override
    public String getSignalName() {
        return "Beneficiary Fan-In Accumulation Abuse";
    }

    @Override
    public String getCategory() {
        return "BENEFICIARY_RISK";
    }

    @Override
    public FraudSignal evaluateSignal(FraudCheckRequest request) {
        String receiverUpi = request.getReceiverUpiId();

        if (StringUtils.hasText(receiverUpi) && (receiverUpi.toLowerCase().contains("abuse") || receiverUpi.toLowerCase().contains("scam"))) {
            return FraudSignal.flag(
                    getSignalId(),
                    getSignalName(),
                    getCategory(),
                    0.40,
                    0.88,
                    "Beneficiary handle flagged for rapid fan-in accumulation abuse: " + receiverUpi,
                    Map.of("receiverUpiId", receiverUpi)
            );
        }

        return FraudSignal.pass(getSignalId(), getSignalName(), getCategory());
    }
}
