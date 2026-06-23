package com.upimesh.risk.service;

import com.upimesh.risk.model.dto.AmountResult;
import com.upimesh.risk.model.dto.DeviceResult;
import com.upimesh.risk.model.dto.LocationResult;
import com.upimesh.risk.model.dto.TimeResult;
import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.entity.RiskScoringResult;
import com.upimesh.risk.model.enums.RiskFactor;
import com.upimesh.risk.model.enums.RiskLevel;
import com.upimesh.risk.model.request.RiskScoringRequest;
import com.upimesh.risk.model.response.RiskScoringResponse;
import com.upimesh.risk.repository.RiskProfileRepository;
import com.upimesh.risk.repository.RiskScoringResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    private final DeviceFingerprintService deviceFingerprintService;
    private final LocationAnalysisService locationAnalysisService;
    private final BehavioralAnalysisService behavioralAnalysisService;
    private final RiskProfileUpdater riskProfileUpdater;

    @Value("${risk.threshold.low:0.3}")
    private double lowThreshold;

    @Value("${risk.threshold.high:0.7}")
    private double highThreshold;

    @Transactional
    public RiskScoringResponse scoreTransaction(RiskScoringRequest request) {
        String scoringId = UUID.randomUUID().toString();
        log.info("Calculating risk score for transaction: {} of user: {}", request.getTransactionId(), request.getUserId());

        // 1. Get or build temporary baseline profile
        RiskProfile profile = profileRepository.findByUserId(request.getUserId())
                .orElse(null);
        double baseScore = profile != null ? profile.getBaseRiskScore() : 0.1;

        double finalScore = baseScore;
        List<RiskFactor> factorsTriggered = new ArrayList<>();

        // 2. Device Fingerprint Check
        DeviceResult deviceResult = deviceFingerprintService.analyzeDevice(request.getDeviceId(), request.getUserId());
        finalScore += deviceResult.scoreContribution();
        if (deviceResult.isNew()) {
            factorsTriggered.add(RiskFactor.NEW_DEVICE);
        }

        // 3. Location Anomaly Check
        LocationResult locationResult = locationAnalysisService.analyzeLocation(request.getCity(), request.getUserId());
        finalScore += locationResult.scoreContribution();
        if (locationResult.isAnomaly()) {
            factorsTriggered.add(RiskFactor.LOCATION_ANOMALY);
        }

        // 4. Time Pattern Check
        int currentHour = LocalDateTime.now().getHour();
        TimeResult timeResult = behavioralAnalysisService.analyzeTimePattern(currentHour, request.getUserId());
        finalScore += timeResult.scoreContribution();
        if (timeResult.isUnusual()) {
            factorsTriggered.add(RiskFactor.UNUSUAL_HOUR);
        }

        // 5. Amount Pattern Check
        AmountResult amountResult = behavioralAnalysisService.analyzeAmountPattern(request.getAmount(), request.getUserId());
        finalScore += amountResult.scoreContribution();
        if (amountResult.isUnusual()) {
            factorsTriggered.add(RiskFactor.VELOCITY_HIGH);
        }

        // 6. High Value Check (> ₹50,000)
        boolean isLargeAmount = request.getAmount().compareTo(BigDecimal.valueOf(50000)) > 0;
        if (isLargeAmount) {
            finalScore += 0.1;
            factorsTriggered.add(RiskFactor.LARGE_AMOUNT);
        }

        // 7. New Receiver Check
        List<RiskScoringResult> pastResults = scoringResultRepository.findByUserIdOrderByScoredAtDesc(request.getUserId());
        boolean isNewReceiver = pastResults.stream()
                .noneMatch(r -> request.getReceiverUpiId().equals(r.getReceiverUpiId()));
        if (isNewReceiver && !pastResults.isEmpty()) {
            finalScore += 0.1;
            factorsTriggered.add(RiskFactor.NEW_RECEIVER);
        }

        // Cap risk score at 1.0
        if (finalScore > 1.0) {
            finalScore = 1.0;
        }

        // Determine Risk Level
        RiskLevel riskLevel;
        if (finalScore < lowThreshold) {
            riskLevel = RiskLevel.LOW;
        } else if (finalScore < 0.5) {
            riskLevel = RiskLevel.MEDIUM;
        } else if (finalScore < highThreshold) {
            riskLevel = RiskLevel.HIGH;
        } else {
            riskLevel = RiskLevel.CRITICAL;
        }

        boolean allowed = finalScore < highThreshold;

        // Save Scoring Result
        RiskScoringResult result = RiskScoringResult.builder()
                .scoringId(scoringId)
                .transactionId(request.getTransactionId())
                .userId(request.getUserId())
                .userUpiId(request.getUserUpiId())
                .receiverUpiId(request.getReceiverUpiId())
                .finalScore(finalScore)
                .riskLevel(riskLevel)
                .factorsTriggered(factorsTriggered)
                .deviceId(request.getDeviceId())
                .ipAddress(request.getIpAddress())
                .hour(currentHour)
                .dayOfWeek(LocalDateTime.now().getDayOfWeek().getValue())
                .isNewDevice(deviceResult.isNew())
                .locationAnomaly(locationResult.isAnomaly())
                .velocityHigh(amountResult.isUnusual())
                .build();
        scoringResultRepository.save(result);

        // Async update RiskProfile
        riskProfileUpdater.updateRiskProfile(request.getUserId(), request, allowed);

        // Map triggers to string list for response payload
        List<String> triggers = factorsTriggered.stream()
                .map(Enum::name)
                .toList();

        return RiskScoringResponse.builder()
                .transactionId(request.getTransactionId())
                .scoringId(scoringId)
                .finalScore(finalScore)
                .riskLevel(riskLevel)
                .allowed(allowed)
                .factorsTriggered(triggers)
                .scoredAt(LocalDateTime.now())
                .build();
    }

    public RiskProfile getUserRiskProfile(String userId) {
        return profileRepository.findByUserId(userId).orElse(null);
    }
}
