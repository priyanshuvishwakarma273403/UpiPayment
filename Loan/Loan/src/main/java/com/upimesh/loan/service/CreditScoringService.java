package com.upimesh.loan.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Service
@Slf4j
public class CreditScoringService {

    private final Random random = new Random();

    /**
     * Mocks a CIBIL credit score check. Returns a random score between 550 and 900.
     */
    public int generateMockScore(String userId) {
        log.info("Generating mock credit score for user: {}", userId);
        return 550 + random.nextInt(351);
    }

    /**
     * Determines the credit decision based on the CIBIL score.
     */
    public CreditDecision scoreToCreditDecision(int score, BigDecimal requestedAmount) {
        log.info("Evaluating credit decision for score: {} and requested amount: ₹{}", score, requestedAmount);

        if (score > 750) {
            return CreditDecision.builder()
                    .approved(true)
                    .approvedAmount(requestedAmount)
                    .interestRate(BigDecimal.valueOf(12.00)) // 12% p.a.
                    .rejectionReason(null)
                    .build();
        } else if (score >= 650) {
            BigDecimal approvedAmount = requestedAmount.multiply(BigDecimal.valueOf(0.70))
                    .setScale(2, RoundingMode.HALF_UP);
            return CreditDecision.builder()
                    .approved(true)
                    .approvedAmount(approvedAmount)
                    .interestRate(BigDecimal.valueOf(18.00)) // 18% p.a.
                    .rejectionReason(null)
                    .build();
        } else {
            return CreditDecision.builder()
                    .approved(false)
                    .approvedAmount(BigDecimal.ZERO)
                    .interestRate(BigDecimal.ZERO)
                    .rejectionReason("CIBIL score is too low (" + score + "). Minimum score of 650 is required.")
                    .build();
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreditDecision {
        private boolean approved;
        private BigDecimal approvedAmount;
        private BigDecimal interestRate;
        private String rejectionReason;
    }
}
