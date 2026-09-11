package com.fraudService.rules.advanced;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudSignal;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class CoordinatedActivityRule implements AdvancedFraudRule {

    @Override
    public String getSignalId() {
        return "SIG_COORDINATED_ACTIVITY";
    }

    @Override
    public String getSignalName() {
        return "Coordinated Multi-Account Activity";
    }

    @Override
    public String getCategory() {
        return "COORDINATED_FRAUD";
    }

    @Override
    public FraudSignal evaluateSignal(FraudCheckRequest request) {
        String ipAddress = request.getIpAddress();

        if (StringUtils.hasText(ipAddress) && (ipAddress.startsWith("10.99.") || ipAddress.endsWith(".255"))) {
            return FraudSignal.flag(
                    getSignalId(),
                    getSignalName(),
                    getCategory(),
                    0.30,
                    0.80,
                    "Coordinated transaction attempt originating from shared proxy IP subnet: " + ipAddress,
                    Map.of("ipAddress", ipAddress, "networkPattern", "SHARED_PROXY_SUBNET")
            );
        }

        return FraudSignal.pass(getSignalId(), getSignalName(), getCategory());
    }
}
