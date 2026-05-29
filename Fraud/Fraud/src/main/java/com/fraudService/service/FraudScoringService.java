package com.fraudService.service;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.repository.FraudLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * ================================================================
 * AI Fraud Scoring Service
 * ================================================================
 * Abhi yeh rule-based scoring implement karta hai.
 * Production mein yahan ML model (Python FastAPI) call hoga.
 *
 * Scoring factors:
 * 1. Transaction amount vs user's typical amount (30% weight)
 * 2. Fraud history of sender (25% weight)
 * 3. New receiver (15% weight)
 * 4. Time of day (10% weight)
 * 5. Transaction velocity (20% weight)
 *
 * Future: Spring AI / external ML model REST call
 * ================================================================
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudScoringService {

    private final FraudLogRepository fraudLogRepository;

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("25000");
    private static final BigDecimal VERY_HIGH_AMOUNT_THRESHOLD = new BigDecimal("75000");


    /**
     * AI fraud score calculate karo (0.0 = safe, 1.0 = definitely fraud)
     */
    public double calculateFraudScore(FraudCheckRequest request){

        double score = 0.0;

        // Factor 1: Amount analysis (30% weight)
        score += calculateAmountScore(request.getAmount()) * 0.30;

        // Factor 2: Sender fraud history (25% weight)
        score += calculateHistoryScore(request.getSenderId()) * 0.25;

        // Factor 3: New vs known receiver (15% weight)
        score += calculateReceiverScore(request.getSenderId(), request.getReceiverUpiId()) * 0.15;

        // Factor 4: Time of day (10% weight)
        score += calculateTimeScore() * 0.10;

        // Factor 5: Transaction velocity from Redis (20% weight)
        // (Redis check RapidTransactionRule mein hota hai, yahan simulate)
        score += 0.0 * 0.20; // Placeholder

        // Cap at 1.0
        score = Math.min(score, 1.0);

        log.debug("AI fraud score for payment={}: {}", request.getPaymentId(), score);
        return Math.round(score * 100.0) / 100.0;

    }

    /** Amount-based scoring */
    private double calculateAmountScore(BigDecimal amount){
        if(amount.compareTo(VERY_HIGH_AMOUNT_THRESHOLD) >= 0) return 0.8;
        if(amount.compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) return 0.5;
        if(amount.compareTo(new BigDecimal("10000")) >= 0) return 0.3;
        return 0.0;
    }

    /** Sender ka fraud history score */
    private double calculateHistoryScore(Long senderId){
        try {
            long blockedCount = fraudLogRepository.countBySenderIdAndFinalDecisionAndCheckedAtAfter(
                    senderId, "BLOCKED", LocalDateTime.now().minusDays(30));
            if (blockedCount >= 3)  return 0.9;
            if (blockedCount >= 1)  return 0.5;

            long reviewCount = fraudLogRepository.countBySenderIdAndFinalDecisionAndCheckedAtAfter(
                    senderId, "REVIEW", LocalDateTime.now().minusDays(7));
            if (reviewCount >= 2)   return 0.4;
            return 0.0;
        } catch (Exception e) {
            log.warn("Could not fetch fraud history for sender {}: {}", senderId, e.getMessage());
            return 0.0;
        }
    }

    /** Receiver new hai ya known */
    private double calculateReceiverScore(Long senderId, String receiverUpiId){
        try{
            // Agar pehle kabhi is receiver ko payment ki hai, low risk
            long previousTxns = fraudLogRepository
                    .findRepeatedTransactions(senderId, receiverUpiId,
                            BigDecimal.ZERO, LocalDateTime.now().minusDays(90))
                    .size();
            return previousTxns > 0 ? 0.0 : 0.2; //  New receiver = slightly higher risk
        }catch(Exception e){
            return 0.1;
        }
    }

    /** Time-based risk */
    private double calculateTimeScore() {
        int hour = LocalDateTime.now().getHour();
        // 1AM - 5AM = higher risk
        if (hour >= 1 && hour <= 5) return 0.4;
        // 11PM - 1AM = moderate risk
        if (hour >= 23 || hour == 0) return 0.2;
        return 0.0;
    }
}
