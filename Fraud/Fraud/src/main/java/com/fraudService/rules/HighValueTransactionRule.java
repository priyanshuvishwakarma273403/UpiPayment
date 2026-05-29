package com.fraudService.rules;

import com.fraudService.dto.request.FraudCheckRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * ================================================================
 * High Value Transaction Rule
 * ================================================================
 * 50,000 se zyada amount = Review
 * 1,00,000 se zyada amount = Blocked (extra verification chahiye)
 */
@Component
@RequiredArgsConstructor
public class HighValueTransactionRule implements FraudRule{

    private static final BigDecimal REVIEW_THRESHOLD = new BigDecimal("50000");
    private static final BigDecimal BLOCK_THRESHOLD = new BigDecimal("100000");

    @Override
    public RuleResult evaluate(FraudCheckRequest request) {

        BigDecimal amount = request.getAmount();
        if(amount.compareTo(BLOCK_THRESHOLD) >= 0){
            return RuleResult.blocked("HighValueRule",
                    "Transaction exceeds ₹1,00,000 limit: ₹" + amount);
        }

        if(amount.compareTo(REVIEW_THRESHOLD) >= 0){
            return RuleResult.review("HighValueRule",
                    "High value transaction: ₹" + amount, 0.5);
        }

        return RuleResult.safe("HighValueRule");
    }
}
