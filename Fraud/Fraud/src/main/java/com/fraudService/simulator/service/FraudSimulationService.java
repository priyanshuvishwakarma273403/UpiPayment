package com.fraudService.simulator.service;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.request.MlPredictionRequest;
import com.fraudService.dto.response.FraudSignal;
import com.fraudService.rules.FraudRule;
import com.fraudService.rules.advanced.AdvancedFraudRule;
import com.fraudService.service.FraudScoringService;
import com.fraudService.simulator.model.FraudSimulationRequest;
import com.fraudService.simulator.model.FraudSimulationResponse;
import com.fraudService.simulator.model.RuleTriggerDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Core engine for Phase 19 Fraud Attack Simulator.
 * Runs analyst simulations using production risk components (baseline rules, advanced rules,
 * behavioral risk, graph risk, ML probability) without persisting state or mutating database records.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FraudSimulationService {

    private final List<FraudRule> baselineRules;
    private final List<AdvancedFraudRule> advancedRules;
    private final FraudScoringService aiScoringService;

    public FraudSimulationResponse runSimulation(FraudSimulationRequest simReq) {
        long startTimeMs = System.currentTimeMillis();
        log.info("[SIMULATION MODE] Running fraud attack simulation for customerId={}, amount={}",
                simReq.getSimulatedCustomerId(), simReq.getTransactionAmount());

        // 1. Construct production FraudCheckRequest context
        FraudCheckRequest checkReq = mapToFraudCheckRequest(simReq);

        // 2. Evaluate rules triggered
        List<RuleTriggerDetail> rulesTriggered = new ArrayList<>();
        StringBuilder explanationBuilder = new StringBuilder();

        // Baseline production rules
        for (FraudRule rule : baselineRules) {
            try {
                FraudRule.RuleResult res = rule.evaluate(checkReq);
                if (res.getDecision() != FraudRule.Decision.SAFE) {
                    RuleTriggerDetail detail = RuleTriggerDetail.builder()
                            .ruleId(res.getRuleName())
                            .ruleName(res.getRuleName())
                            .category("BASELINE")
                            .scoreContribution(res.getRiskScore())
                            .reason(res.getReason())
                            .build();
                    rulesTriggered.add(detail);

                    if (!explanationBuilder.isEmpty()) explanationBuilder.append("; ");
                    explanationBuilder.append(res.getRuleName()).append(": ").append(res.getReason());
                }
            } catch (Exception e) {
                log.warn("[SIMULATION MODE] Error evaluating baseline rule: {}", e.getMessage());
            }
        }

        // Advanced production rules
        for (AdvancedFraudRule advRule : advancedRules) {
            try {
                FraudSignal signal = advRule.evaluateSignal(checkReq);
                if (signal.isTriggered()) {
                    RuleTriggerDetail detail = RuleTriggerDetail.builder()
                            .ruleId(signal.getSignalId())
                            .ruleName(signal.getSignalName())
                            .category(signal.getCategory())
                            .scoreContribution(signal.getScoreContribution())
                            .reason(signal.getExplanation())
                            .build();
                    rulesTriggered.add(detail);

                    if (!explanationBuilder.isEmpty()) explanationBuilder.append("; ");
                    explanationBuilder.append(signal.getSignalName()).append(": ").append(signal.getExplanation());
                }
            } catch (Exception e) {
                log.warn("[SIMULATION MODE] Error evaluating advanced rule {}: {}", advRule.getSignalId(), e.getMessage());
            }
        }

        // Additional input-based synthetic rules for simulator parameters
        evaluateSimulationSpecificRules(simReq, rulesTriggered, explanationBuilder);

        // 3. Compute Behavioral Risk
        double behavioralRisk = calculateBehavioralRisk(simReq);

        // 4. Compute Graph Risk
        double graphRisk = calculateGraphRisk(simReq);

        // 5. Compute ML Probability
        double mlProbability = calculateMlProbability(checkReq, simReq);

        // 6. Compute Composite Final Risk matching production FraudIntelligenceService
        double compositeScore = 0.0;
        for (RuleTriggerDetail rule : rulesTriggered) {
            compositeScore += rule.getScoreContribution();
        }
        compositeScore += (0.15 * behavioralRisk) + (0.15 * graphRisk);
        if (mlProbability > 0.5) {
            compositeScore += (mlProbability * 0.25);
        }

        double finalRisk = Math.min(1.0, Math.max(0.0, compositeScore));
        finalRisk = Math.round(finalRisk * 1000.0) / 1000.0;

        // 7. Determine Risk Level & Decision
        String riskLevel;
        String decision;

        if (finalRisk < 0.25) {
            riskLevel = "LOW_RISK";
            decision = "ALLOWED";
        } else if (finalRisk < 0.55) {
            riskLevel = "SUSPICIOUS";
            decision = "REVIEW";
        } else if (finalRisk < 0.80) {
            riskLevel = "HIGH_RISK";
            decision = "REVIEW";
        } else {
            riskLevel = "REQUIRES_INVESTIGATION";
            decision = "BLOCKED";
        }

        String finalExplanation = explanationBuilder.isEmpty()
                ? "Simulated transaction pattern clean. Low risk detected."
                : explanationBuilder.toString();

        long processingTimeMs = System.currentTimeMillis() - startTimeMs;

        // 8. Return response strictly labelled with SIMULATION MODE
        return FraudSimulationResponse.builder()
                .simulationMode(true)
                .modeLabel("SIMULATION MODE")
                .rulesTriggered(rulesTriggered)
                .behavioralRisk(Math.round(behavioralRisk * 1000.0) / 1000.0)
                .graphRisk(Math.round(graphRisk * 1000.0) / 1000.0)
                .mlProbability(Math.round(mlProbability * 1000.0) / 1000.0)
                .finalRisk(finalRisk)
                .riskLevel(riskLevel)
                .decision(decision)
                .explanation(finalExplanation)
                .simulatedAt(LocalDateTime.now())
                .processingTimeMs(processingTimeMs)
                .build();
    }

    private FraudCheckRequest mapToFraudCheckRequest(FraudSimulationRequest simReq) {
        double velocity = simReq.getVelocity() != null ? simReq.getVelocity() : 1.0;
        double ipRisk = simReq.getIpRiskScore() != null ? simReq.getIpRiskScore() : 0.0;
        double devAge = simReq.getDeviceAgeDays() != null ? simReq.getDeviceAgeDays() : 30.0;

        String paymentId = "SIM_TXN_" + System.currentTimeMillis();
        if (velocity > 5.0) {
            paymentId = "SIM_BURST_" + System.currentTimeMillis();
        }

        String deviceId = simReq.getSimulatedDeviceId();
        if (devAge < 1.0) {
            deviceId = "NEW_DEV_SIM_" + System.currentTimeMillis();
        }

        return FraudCheckRequest.builder()
                .paymentId(paymentId)
                .senderId(9999L)
                .receiverId(8888L)
                .senderUpiId(simReq.getSimulatedCustomerId() + "@upi")
                .receiverUpiId(simReq.getSimulatedMerchantId() + "@upi")
                .amount(simReq.getTransactionAmount() != null ? simReq.getTransactionAmount() : BigDecimal.ZERO)
                .deviceId(deviceId)
                .ipAddress(simReq.getSimulatedIpAddress())
                .paymentMode("UPI_INTENT")
                .build();
    }

    private void evaluateSimulationSpecificRules(FraudSimulationRequest simReq,
                                                  List<RuleTriggerDetail> rulesTriggered,
                                                  StringBuilder explanationBuilder) {
        if (simReq.getVelocity() != null && simReq.getVelocity() > 5.0) {
            rulesTriggered.add(RuleTriggerDetail.builder()
                    .ruleId("RULE_SIM_HIGH_VELOCITY")
                    .ruleName("Simulated Velocity Burst")
                    .category("VELOCITY")
                    .scoreContribution(0.40)
                    .reason("Simulated transaction velocity exceeds burst threshold: " + simReq.getVelocity() + " txns/min")
                    .build());
            appendExplanation(explanationBuilder, "Simulated High Velocity Burst (" + simReq.getVelocity() + "/min)");
        }

        if (simReq.getDeviceAgeDays() != null && simReq.getDeviceAgeDays() < 1.0) {
            rulesTriggered.add(RuleTriggerDetail.builder()
                    .ruleId("RULE_SIM_NEW_DEVICE")
                    .ruleName("Simulated New Unrecognized Device")
                    .category("DEVICE")
                    .scoreContribution(0.35)
                    .reason("Simulated payment initiated from new unrecognized device (Age: " + simReq.getDeviceAgeDays() + " days)")
                    .build());
            appendExplanation(explanationBuilder, "Simulated New Device (Age < 1 day)");
        }

        if (simReq.getIpRiskScore() != null && simReq.getIpRiskScore() > 0.6) {
            rulesTriggered.add(RuleTriggerDetail.builder()
                    .ruleId("RULE_SIM_HIGH_IP_RISK")
                    .ruleName("Simulated High Risk IP Network")
                    .category("NETWORK")
                    .scoreContribution(0.30)
                    .reason("Simulated connection from high risk IP pool (IP Risk Score: " + simReq.getIpRiskScore() + ")")
                    .build());
            appendExplanation(explanationBuilder, "Simulated High Risk IP Pool");
        }

        if (simReq.getLocationDeviation() != null && simReq.getLocationDeviation() > 50.0) {
            rulesTriggered.add(RuleTriggerDetail.builder()
                    .ruleId("RULE_SIM_LOCATION_ANOMALY")
                    .ruleName("Simulated Geolocation Anomaly")
                    .category("LOCATION")
                    .scoreContribution(0.25)
                    .reason("Simulated location deviation exceeds threshold: " + simReq.getLocationDeviation() + " km")
                    .build());
            appendExplanation(explanationBuilder, "Simulated Geolocation Anomaly (" + simReq.getLocationDeviation() + "km)");
        }
    }

    private void appendExplanation(StringBuilder sb, String text) {
        if (!sb.isEmpty()) sb.append("; ");
        sb.append(text);
    }

    private double calculateBehavioralRisk(FraudSimulationRequest simReq) {
        double velocity = simReq.getVelocity() != null ? simReq.getVelocity() : 1.0;
        double velScore = velocity > 10.0 ? 0.9 : (velocity > 5.0 ? 0.6 : (velocity > 2.0 ? 0.3 : 0.0));

        double locDev = simReq.getLocationDeviation() != null ? simReq.getLocationDeviation() : 0.0;
        double locScore = locDev > 100.0 ? 0.85 : (locDev > 50.0 ? 0.55 : (locDev > 10.0 ? 0.25 : 0.0));

        double accAge = simReq.getAccountAgeDays() != null ? simReq.getAccountAgeDays() : 180.0;
        double accAgeScore = accAge < 2.0 ? 0.8 : (accAge < 7.0 ? 0.5 : (accAge < 30.0 ? 0.25 : 0.0));

        double benAge = simReq.getBeneficiaryAgeDays() != null ? simReq.getBeneficiaryAgeDays() : 90.0;
        double benAgeScore = benAge < 0.1 ? 0.85 : (benAge < 1.0 ? 0.5 : (benAge < 7.0 ? 0.25 : 0.0));

        double score = (0.35 * velScore) + (0.25 * locScore) + (0.20 * accAgeScore) + (0.20 * benAgeScore);
        return Math.min(1.0, score);
    }

    private double calculateGraphRisk(FraudSimulationRequest simReq) {
        double devAge = simReq.getDeviceAgeDays() != null ? simReq.getDeviceAgeDays() : 30.0;
        double devAgeScore = devAge < 1.0 ? 0.85 : (devAge < 7.0 ? 0.45 : (devAge < 30.0 ? 0.15 : 0.0));

        double ipRisk = simReq.getIpRiskScore() != null ? simReq.getIpRiskScore() : 0.0;
        double merchantRisk = simReq.getMerchantRiskScore() != null ? simReq.getMerchantRiskScore() : 0.0;

        double score = (0.40 * devAgeScore) + (0.35 * ipRisk) + (0.25 * merchantRisk);
        return Math.min(1.0, score);
    }

    private double calculateMlProbability(FraudCheckRequest checkReq, FraudSimulationRequest simReq) {
        try {
            double mlScore = aiScoringService.calculateFraudScore(checkReq);
            if (mlScore > 0.0) {
                return mlScore;
            }
        } catch (Exception e) {
            log.warn("[SIMULATION MODE] ML service score call exception: {}", e.getMessage());
        }

        // Feature-based model formula fallback
        double amount = simReq.getTransactionAmount() != null ? simReq.getTransactionAmount().doubleValue() : 0.0;
        double amountDevScore = amount > 75000 ? 0.85 : (amount > 25000 ? 0.55 : (amount > 10000 ? 0.25 : 0.05));
        double velocityScore = simReq.getVelocity() != null && simReq.getVelocity() > 5 ? 0.75 : 0.1;
        double ipRisk = simReq.getIpRiskScore() != null ? simReq.getIpRiskScore() : 0.1;

        double prob = (0.40 * amountDevScore) + (0.35 * velocityScore) + (0.25 * ipRisk);
        return Math.min(0.99, Math.max(0.01, Math.round(prob * 100.0) / 100.0));
    }
}
