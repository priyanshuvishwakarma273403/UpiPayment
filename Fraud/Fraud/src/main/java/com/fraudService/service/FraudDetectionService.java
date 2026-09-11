package com.fraudService.service;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudCheckResponse;
import com.fraudService.dto.response.FraudIntelligenceResult;
import com.fraudService.dto.response.FraudSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final FraudIntelligenceService intelligenceService;

    public FraudCheckResponse checkFraud(FraudCheckRequest request) {
        FraudIntelligenceResult intelResult = intelligenceService.evaluateFraudIntelligence(request);

        FraudCheckResponse.FraudDecision legacyDecision;
        if ("BLOCKED".equalsIgnoreCase(intelResult.getLegacyDecision())) {
            legacyDecision = FraudCheckResponse.FraudDecision.BLOCKED;
        } else if ("REVIEW".equalsIgnoreCase(intelResult.getLegacyDecision())) {
            legacyDecision = FraudCheckResponse.FraudDecision.REVIEW;
        } else {
            legacyDecision = FraudCheckResponse.FraudDecision.SAFE;
        }

        List<FraudCheckResponse.RuleDetail> ruleDetails = intelResult.getFraudSignals().stream()
                .map(s -> FraudCheckResponse.RuleDetail.builder()
                        .ruleName(s.getSignalName())
                        .decision(s.isTriggered() ? "FLAGGED" : "SAFE")
                        .reason(s.getExplanation())
                        .score(s.getScoreContribution())
                        .build())
                .toList();

        return FraudCheckResponse.builder()
                .paymentId(request.getPaymentId())
                .decision(legacyDecision)
                .intelligenceDecision(intelResult.getDecision())
                .riskLevel(intelResult.getRiskLevel().name())
                .riskScore(intelResult.getRiskScore())
                .allowed(intelResult.isAllowed())
                .reasons(intelResult.getExplanation())
                .ruleDetails(ruleDetails)
                .checkedAt(LocalDateTime.now())
                .processingTimeMs(intelResult.getProcessingTimeMs())
                .build();
    }
}
