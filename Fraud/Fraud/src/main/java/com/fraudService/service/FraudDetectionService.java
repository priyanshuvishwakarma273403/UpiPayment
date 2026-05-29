package com.fraudService.service;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudCheckResponse;
import com.fraudService.entity.FraudLog;
import com.fraudService.repository.FraudLogRepository;
import com.fraudService.rules.FraudRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final List<FraudRule>  fraudRules;
    private final FraudScoringService aiScoringService;
    private final FraudLogRepository fraudLogRepository;

    public FraudCheckResponse checkFraud(FraudCheckRequest request){
        long startTime = System.currentTimeMillis();
        log.info("Fraud check: paymentId={}, amount={}", request.getPaymentId(), request.getAmount());

        List<FraudCheckResponse.RuleDetail> ruleDetails = new ArrayList<>();
        int blockedCount = 0;
        int reviewCount = 0;
        double totalRuleScore = 0.0;
        StringBuilder reasons = new StringBuilder();

        for(FraudRule rule : fraudRules){
            try{
                FraudRule.RuleResult result = rule.evaluate(request);
                ruleDetails.add(FraudCheckResponse.RuleDetail.builder()
                        .ruleName(result.getRuleName())
                        .decision(result.getDecision().name())
                        .reason(result.getReason())
                        .score(result.getRiskScore())
                        .build());
                totalRuleScore += result.getRiskScore();
                switch (result.getDecision()) {
                    case BLOCKED -> { blockedCount++; reasons.append("[BLOCKED] ").append(result.getReason()).append("; "); }
                    case REVIEW  -> { reviewCount++;  reasons.append("[REVIEW] ").append(result.getReason()).append("; "); }
                    default -> {}
                }
            }catch(Exception e){
                log.error("Rule {} failed: {}", rule.getClass().getSimpleName(), e.getMessage());
            }
        }

        double aiScore = aiScoringService.calculateFraudScore(request);
        double avgRuleScore = fraudRules.isEmpty() ? 0.0 : totalRuleScore / fraudRules.size();
        double compositeScore = Math.min((avgRuleScore + aiScore) / 2.0, 1.0);

        if (aiScore >= 0.7) reasons.append("[AI] High fraud probability: ").append(String.format("%.2f", aiScore)).append("; ");

        FraudCheckResponse.FraudDecision finalDecision;

        if (blockedCount > 0 || aiScore >= 0.7)     finalDecision = FraudCheckResponse.FraudDecision.BLOCKED;
        else if (reviewCount >= 2 || aiScore >= 0.4) finalDecision = FraudCheckResponse.FraudDecision.REVIEW;
        else                                          finalDecision = FraudCheckResponse.FraudDecision.SAFE;

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("Fraud result: paymentId={}, decision={}, score={}", request.getPaymentId(), finalDecision, compositeScore);

        saveFraudLog(request, finalDecision, compositeScore, aiScore, ruleDetails, reasons.toString(), processingTime);

        return FraudCheckResponse.builder()
                .paymentId(request.getPaymentId())
                .decision(finalDecision)
                .riskScore(Math.round(compositeScore * 100.0) / 100.0)
                .aiScore(Math.round(aiScore * 100.0) / 100.0)
                .reasons(reasons.toString())
                .ruleDetails(ruleDetails)
                .checkedAt(LocalDateTime.now())
                .processingTimeMs(processingTime)
                .build();
    }

    private void saveFraudLog(FraudCheckRequest req, FraudCheckResponse.FraudDecision decision,
                              double compositeScore, double aiScore, List<FraudCheckResponse.RuleDetail> ruleDetails,
                              String reasons, long processingTime) {
        try {
            FraudLog log2 = FraudLog.builder()
                    .paymentId(req.getPaymentId()).senderId(req.getSenderId())
                    .receiverId(req.getReceiverId()).senderUpiId(req.getSenderUpiId())
                    .receiverUpiId(req.getReceiverUpiId()).amount(req.getAmount())
                    .paymentMode(req.getPaymentMode()).finalDecision(decision.name())
                    .riskScore(compositeScore).aiScore(aiScore)
                    .reasons(reasons)
                    .actionTaken(decision == FraudCheckResponse.FraudDecision.BLOCKED ? "BLOCKED"
                            : decision == FraudCheckResponse.FraudDecision.REVIEW ? "FLAGGED_FOR_REVIEW" : "ALLOWED")
                    .checkedAt(LocalDateTime.now()).processingTimeMs(processingTime)
                    .ruleResults(ruleDetails.stream().map(r -> FraudLog.RuleResult.builder()
                            .ruleName(r.getRuleName()).decision(r.getDecision())
                            .reason(r.getReason()).riskScore(r.getScore()).build()).toList())
                    .build();
            fraudLogRepository.save(log2);
        } catch (Exception e) {
            log.error("Failed to save fraud log: {}", e.getMessage());
        }
    }
}
