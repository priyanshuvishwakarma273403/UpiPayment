package com.fraudService.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudCheckResponse;
import com.fraudService.dto.response.FraudIntelligenceResult;
import com.fraudService.dto.response.FraudRiskLevel;
import com.fraudService.dto.response.FraudSignal;
import com.fraudService.entity.FraudLog;
import com.fraudService.repository.FraudLogRepository;
import com.fraudService.rules.FraudRule;
import com.fraudService.rules.advanced.AdvancedFraudRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudIntelligenceService {

    private final List<FraudRule> baselineRules;
    private final List<AdvancedFraudRule> advancedRules;
    private final FraudScoringService aiScoringService;
    private final FraudLogRepository fraudLogRepository;
    private final ObjectMapper objectMapper;

    public FraudIntelligenceResult evaluateFraudIntelligence(FraudCheckRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Running Fraud Intelligence Engine for paymentId={}, senderId={}", request.getPaymentId(), request.getSenderId());

        List<FraudSignal> signals = new ArrayList<>();
        List<FraudLog.RuleResult> ruleResults = new ArrayList<>();
        Map<String, Object> compositeEvidence = new HashMap<>();

        double compositeScore = 0.0;
        StringBuilder explanationBuilder = new StringBuilder();

        // 1. Evaluate baseline rules
        for (FraudRule rule : baselineRules) {
            try {
                FraudRule.RuleResult res = rule.evaluate(request);
                ruleResults.add(FraudLog.RuleResult.builder()
                        .ruleName(res.getRuleName())
                        .decision(res.getDecision().name())
                        .reason(res.getReason())
                        .riskScore(res.getRiskScore())
                        .build());

                if (res.getDecision() != FraudRule.Decision.SAFE) {
                    compositeScore += res.getRiskScore() * 0.3;
                    if (!explanationBuilder.isEmpty()) explanationBuilder.append("; ");
                    explanationBuilder.append(res.getRuleName()).append(": ").append(res.getReason());
                }
            } catch (Exception e) {
                log.error("Error evaluating baseline rule: {}", e.getMessage());
            }
        }

        // 2. Evaluate advanced intelligence signals
        for (AdvancedFraudRule advRule : advancedRules) {
            try {
                FraudSignal signal = advRule.evaluateSignal(request);
                signals.add(signal);

                if (signal.isTriggered()) {
                    compositeScore += signal.getScoreContribution();
                    if (!explanationBuilder.isEmpty()) explanationBuilder.append("; ");
                    explanationBuilder.append(signal.getSignalName()).append(": ").append(signal.getExplanation());
                    compositeEvidence.putAll(signal.getEvidenceMap());
                }
            } catch (Exception e) {
                log.error("Error evaluating advanced rule {}: {}", advRule.getSignalId(), e.getMessage());
            }
        }

        // 3. AI Model score integration
        double aiScore = 0.0;
        try {
            aiScore = aiScoringService.calculateFraudScore(request);
            if (aiScore > 0.6) {
                compositeScore += (aiScore * 0.2);
            }
        } catch (Exception e) {
            log.warn("AI scoring failed: {}", e.getMessage());
        }

        // Normalize composite score (0.0 to 1.0)
        compositeScore = Math.min(1.0, Math.max(0.0, compositeScore));

        // 4. Map Risk Level & Decision
        FraudRiskLevel riskLevel;
        String decision;
        String legacyDecision;
        boolean allowed;

        if (compositeScore < 0.25) {
            riskLevel = FraudRiskLevel.LOW_RISK;
            decision = "LOW RISK";
            legacyDecision = "SAFE";
            allowed = true;
        } else if (compositeScore < 0.55) {
            riskLevel = FraudRiskLevel.SUSPICIOUS;
            decision = "SUSPICIOUS";
            legacyDecision = "REVIEW";
            allowed = true;
        } else if (compositeScore < 0.80) {
            riskLevel = FraudRiskLevel.HIGH_RISK;
            decision = "HIGH RISK";
            legacyDecision = "REVIEW";
            allowed = false;
        } else {
            riskLevel = FraudRiskLevel.REQUIRES_INVESTIGATION;
            decision = "REQUIRES INVESTIGATION";
            legacyDecision = "BLOCKED";
            allowed = false;
        }

        String finalExplanation = explanationBuilder.isEmpty()
                ? "Low risk pattern observed. Transaction clean."
                : explanationBuilder.toString();

        long processingTimeMs = System.currentTimeMillis() - startTime;

        // Serialize signals JSON
        String signalsJson;
        try {
            signalsJson = objectMapper.writeValueAsString(signals);
        } catch (Exception e) {
            signalsJson = "[]";
        }

        String investigationStatus = (riskLevel == FraudRiskLevel.REQUIRES_INVESTIGATION || riskLevel == FraudRiskLevel.HIGH_RISK)
                ? "PENDING"
                : "NONE";

        // Save MongoDB audit log
        FraudLog logEntity = FraudLog.builder()
                .paymentId(request.getPaymentId())
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .senderUpiId(request.getSenderUpiId())
                .receiverUpiId(request.getReceiverUpiId())
                .amount(request.getAmount())
                .paymentMode(request.getPaymentMode())
                .deviceId(request.getDeviceId())
                .ipAddress(request.getIpAddress())
                .finalDecision(legacyDecision)
                .intelligenceDecision(decision)
                .riskLevel(riskLevel.name())
                .riskScore(compositeScore)
                .aiScore(aiScore)
                .ruleResults(ruleResults)
                .fraudSignalsJson(signalsJson)
                .evidenceMap(compositeEvidence)
                .investigationStatus(investigationStatus)
                .reasons(finalExplanation)
                .actionTaken(allowed ? "ALLOWED" : "BLOCKED")
                .checkedAt(LocalDateTime.now())
                .processingTimeMs(processingTimeMs)
                .build();

        try {
            fraudLogRepository.save(logEntity);
        } catch (Exception e) {
            log.error("Failed to save FraudLog to MongoDB: {}", e.getMessage());
        }

        return FraudIntelligenceResult.builder()
                .paymentId(request.getPaymentId())
                .riskScore(compositeScore)
                .riskLevel(riskLevel)
                .decision(decision)
                .legacyDecision(legacyDecision)
                .allowed(allowed)
                .explanation(finalExplanation)
                .fraudSignals(signals)
                .evidence(compositeEvidence)
                .evaluatedAt(LocalDateTime.now())
                .processingTimeMs(processingTimeMs)
                .build();
    }
}
