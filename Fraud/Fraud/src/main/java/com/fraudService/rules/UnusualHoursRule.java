package com.fraudService.rules;

import com.fraudService.dto.request.FraudCheckRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * ================================================================
 * Unusual Hours Rule
 * ================================================================
 * Raat 1 AM se 5 AM ke beech badi transactions suspicious hain
 */
@Component
public class UnusualHoursRule implements FraudRule{

    @Override
    public RuleResult evaluate(FraudCheckRequest request) {
        LocalTime now = LocalTime.now();
        LocalTime suspiciousStart = LocalTime.of(1, 0);  // 1 AM
        LocalTime suspiciousEnd = LocalTime.of(5, 0);    // 5 AM

        boolean isUnusualHour = now.isAfter(suspiciousStart) && now.isBefore(suspiciousEnd);
        boolean isHighAmount = request.getAmount().compareTo(new BigDecimal("10000")) >= 0;

        if (isUnusualHour && isHighAmount) {
            return RuleResult.review("UnusualHoursRule",
                    "High value transaction at unusual hour: " + now, 0.45);
        }

        return RuleResult.safe("UnusualHoursRule");
    }


}
