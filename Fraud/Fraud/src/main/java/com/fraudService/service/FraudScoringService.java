package com.fraudService.service;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.request.MlPredictionRequest;
import com.fraudService.dto.response.MlPredictionResponse;
import com.fraudService.repository.FraudLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ================================================================
 * AI & Machine Learning Fraud Scoring Service
 * ================================================================
 * Integrates independent Python FastAPI ML Microservice REST client
 * with fallback heuristic scoring.
 * ML provides a probabilistic risk signal; Java Risk orchestration
 * executes the final controlled decision boundary.
 * ================================================================
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudScoringService {

    private final FraudLogRepository fraudLogRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String ML_SERVICE_PREDICT_URL = "http://localhost:8000/predict";
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("25000");
    private static final BigDecimal VERY_HIGH_AMOUNT_THRESHOLD = new BigDecimal("75000");

    /**
     * Calculate ML fraud score signal (0.0 = safe, 1.0 = high risk)
     */
    public double calculateFraudScore(FraudCheckRequest request) {
        try {
            MlPredictionRequest mlReq = extractFeatures(request);
            MlPredictionResponse mlRes = restTemplate.postForObject(
                    ML_SERVICE_PREDICT_URL, mlReq, MlPredictionResponse.class);

            if (mlRes != null && mlRes.getFraudProbability() != null) {
                log.info("ML Service prediction received for paymentId={}: prob={}, model={}, confidence={}",
                        request.getPaymentId(), mlRes.getFraudProbability(), mlRes.getModelVersion(), mlRes.getConfidence());
                return mlRes.getFraudProbability();
            }
        } catch (Exception e) {
            log.warn("ML Service unavailable/failed for paymentId={}: {}. Falling back to heuristic AI scoring.",
                    request.getPaymentId(), e.getMessage());
        }

        // Heuristic AI Fallback Scoring
        return calculateHeuristicScore(request);
    }

    private MlPredictionRequest extractFeatures(FraudCheckRequest request) {
        double amountDev = calculateAmountScore(request.getAmount());
        double historyScore = calculateHistoryScore(request.getSenderId());
        double receiverScore = calculateReceiverScore(request.getSenderId(), request.getReceiverUpiId());
        double timeScore = calculateTimeScore();

        return MlPredictionRequest.builder()
                .amountDeviation(amountDev * 5.0)
                .velocity1m(1.0)
                .deviceAgeDays(30.0)
                .accountAgeDays(180.0)
                .merchantRiskScore(0.20)
                .beneficiaryHistoryCount(receiverScore < 0.1 ? 5.0 : 0.0)
                .behavioralDeviation(timeScore)
                .graphClusterDensity(historyScore)
                .build();
    }

    private double calculateHeuristicScore(FraudCheckRequest request) {
        double score = 0.0;
        score += calculateAmountScore(request.getAmount()) * 0.30;
        score += calculateHistoryScore(request.getSenderId()) * 0.25;
        score += calculateReceiverScore(request.getSenderId(), request.getReceiverUpiId()) * 0.15;
        score += calculateTimeScore() * 0.10;

        score = Math.min(score, 1.0);
        return Math.round(score * 100.0) / 100.0;
    }

    private double calculateAmountScore(BigDecimal amount) {
        if (amount == null) return 0.0;
        if (amount.compareTo(VERY_HIGH_AMOUNT_THRESHOLD) >= 0) return 0.8;
        if (amount.compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) return 0.5;
        if (amount.compareTo(new BigDecimal("10000")) >= 0) return 0.3;
        return 0.0;
    }

    private double calculateHistoryScore(Long senderId) {
        if (senderId == null) return 0.0;
        try {
            long blockedCount = fraudLogRepository.countBySenderIdAndFinalDecisionAndCheckedAtAfter(
                    senderId, "BLOCKED", LocalDateTime.now().minusDays(30));
            if (blockedCount >= 3) return 0.9;
            if (blockedCount >= 1) return 0.5;

            long reviewCount = fraudLogRepository.countBySenderIdAndFinalDecisionAndCheckedAtAfter(
                    senderId, "REVIEW", LocalDateTime.now().minusDays(7));
            if (reviewCount >= 2) return 0.4;
            return 0.0;
        } catch (Exception e) {
            log.warn("Could not fetch fraud history for sender {}: {}", senderId, e.getMessage());
            return 0.0;
        }
    }

    private double calculateReceiverScore(Long senderId, String receiverUpiId) {
        if (senderId == null || receiverUpiId == null) return 0.1;
        try {
            long previousTxns = fraudLogRepository
                    .findRepeatedTransactions(senderId, receiverUpiId,
                            BigDecimal.ZERO, LocalDateTime.now().minusDays(90))
                    .size();
            return previousTxns > 0 ? 0.0 : 0.2;
        } catch (Exception e) {
            return 0.1;
        }
    }

    private double calculateTimeScore() {
        int hour = LocalDateTime.now().getHour();
        if (hour >= 1 && hour <= 5) return 0.4;
        if (hour >= 23 || hour == 0) return 0.2;
        return 0.0;
    }
}
