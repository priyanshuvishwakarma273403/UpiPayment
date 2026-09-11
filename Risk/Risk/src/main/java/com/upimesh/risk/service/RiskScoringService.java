package com.upimesh.risk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upimesh.risk.config.RiskThresholdConfig;
import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.entity.RiskScoringResult;
import com.upimesh.risk.model.enums.RiskDecision;
import com.upimesh.risk.model.enums.RiskFactor;
import com.upimesh.risk.model.enums.RiskLevel;
import com.upimesh.risk.model.request.RiskScoringRequest;
import com.upimesh.risk.model.response.RiskScoringResponse;
import com.upimesh.risk.repository.RiskProfileRepository;
import com.upimesh.risk.repository.RiskScoringResultRepository;
import com.upimesh.risk.rule.RiskRule;
import com.upimesh.risk.rule.RuleEvaluationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskScoringService {

    private final RiskProfileRepository profileRepository;
    private final RiskScoringResultRepository scoringResultRepository;
    private final List<RiskRule> riskRules;
    private final RiskThresholdConfig thresholdConfig;
    private final ObjectMapper objectMapper;
    private final RiskProfileUpdater riskProfileUpdater;

    @Transactional
    public RiskScoringResponse scoreTransaction(RiskScoringRequest request) {
        String scoringId = UUID.randomUUID().toString();
        log.info("Evaluating real-time risk score for transaction: {} (user: {})", request.getTransactionId(), request.getUserId());

        RiskProfile profile = profileRepository.findByUserId(request.getUserId()).orElse(null);

        double compositeScore = 0.0;
        List<RuleEvaluationResult> ruleDetails = new ArrayList<>();
        List<RiskFactor> factorsTriggered = new ArrayList<>();

        // Evaluate all independent RiskRule strategy beans
        for (RiskRule rule : riskRules) {
            try {
                RuleEvaluationResult result = rule.evaluate(request, profile);
                ruleDetails.add(result);

                if (result.isTriggered()) {
                    compositeScore += result.getScoreContribution();
                    try {
                        RiskFactor factor = RiskFactor.valueOf(result.getRuleId().replace("RULE_", ""));
                        factorsTriggered.add(factor);
                    } catch (Exception ignored) {}
                }
            } catch (Exception e) {
                log.error("Error evaluating rule {}: {}", rule.getRuleId(), e.getMessage());
            }
        }

        // Bound composite score between 0.0 and 100.0
        compositeScore = Math.min(100.0, Math.max(0.0, compositeScore));

        // Determine Risk Decision based on configurable thresholds
        RiskDecision decision;
        if (compositeScore < thresholdConfig.getAllowThreshold()) {
            decision = RiskDecision.ALLOW;
        } else if (compositeScore < thresholdConfig.getMonitorThreshold()) {
            decision = RiskDecision.MONITOR;
        } else if (compositeScore < thresholdConfig.getStepUpThreshold()) {
            decision = RiskDecision.STEP_UP;
        } else if (compositeScore < thresholdConfig.getReviewThreshold()) {
            decision = RiskDecision.REVIEW;
        } else {
            decision = RiskDecision.BLOCK;
        }

        // Map to RiskLevel enum
        RiskLevel riskLevel;
        if (compositeScore < 20.0) {
            riskLevel = RiskLevel.LOW;
        } else if (compositeScore < 50.0) {
            riskLevel = RiskLevel.MEDIUM;
        } else if (compositeScore < 80.0) {
            riskLevel = RiskLevel.HIGH;
        } else {
            riskLevel = RiskLevel.CRITICAL;
        }

        boolean allowed = (decision == RiskDecision.ALLOW || decision == RiskDecision.MONITOR);

        // Serialize rule evaluation breakdown for audit trace
        String ruleDetailsJson;
        try {
            ruleDetailsJson = objectMapper.writeValueAsString(ruleDetails);
        } catch (Exception e) {
            log.warn("Failed to serialize rule details JSON: {}", e.getMessage());
            ruleDetailsJson = "[]";
        }

        int currentHour = LocalDateTime.now().getHour();
        int dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();

        boolean isNewDevice = ruleDetails.stream()
                .anyMatch(r -> "RULE_NEW_DEVICE".equals(r.getRuleId()) && r.isTriggered());
        boolean locationAnomaly = ruleDetails.stream()
                .anyMatch(r -> "RULE_LOCATION_DEVIATION".equals(r.getRuleId()) && r.isTriggered());
        boolean velocityHigh = ruleDetails.stream()
                .anyMatch(r -> "RULE_TRANSACTION_VELOCITY".equals(r.getRuleId()) && r.isTriggered());

        // Save complete RiskScoringResult audit record
        RiskScoringResult auditResult = RiskScoringResult.builder()
                .scoringId(scoringId)
                .transactionId(request.getTransactionId())
                .userId(request.getUserId())
                .userUpiId(request.getUserUpiId())
                .receiverUpiId(request.getReceiverUpiId())
                .finalScore(compositeScore)
                .riskLevel(riskLevel)
                .decision(decision)
                .factorsTriggered(factorsTriggered)
                .ruleDetailsJson(ruleDetailsJson)
                .ruleVersion("v1.0.0")
                .deviceId(request.getDeviceId())
                .ipAddress(request.getIpAddress())
                .hour(currentHour)
                .dayOfWeek(dayOfWeek)
                .isNewDevice(isNewDevice)
                .locationAnomaly(locationAnomaly)
                .velocityHigh(velocityHigh)
                .build();

        scoringResultRepository.save(auditResult);

        // Update customer profile asynchronously
        try {
            riskProfileUpdater.updateRiskProfile(request.getUserId(), request, allowed);
        } catch (Exception e) {
            log.warn("Error updating risk profile for user {}: {}", request.getUserId(), e.getMessage());
        }

        List<String> factorNames = factorsTriggered.stream().map(Enum::name).toList();

        return RiskScoringResponse.builder()
                .transactionId(request.getTransactionId())
                .scoringId(scoringId)
                .finalScore(compositeScore)
                .riskLevel(riskLevel)
                .decision(decision)
                .actionRecommended(decision.name())
                .allowed(allowed)
                .factorsTriggered(factorNames)
                .ruleDetails(ruleDetails)
                .ruleVersion("v1.0.0")
                .scoredAt(LocalDateTime.now())
                .build();
    }

    public RiskProfile getUserRiskProfile(String userId) {
        return profileRepository.findByUserId(userId).orElse(null);
    }
}
