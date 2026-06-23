package com.upimesh.risk.service;

import com.upimesh.risk.model.dto.AmountResult;
import com.upimesh.risk.model.dto.TimeResult;
import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.repository.RiskProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BehavioralAnalysisService {

    private final RiskProfileRepository profileRepository;

    @Value("${risk.night-hours-boost:0.1}")
    private double nightHoursBoost;

    public TimeResult analyzeTimePattern(int hour, String userId) {
        boolean isNightHours = hour >= 2 && hour <= 5;
        double contribution = 0.0;
        boolean isUnusual = false;

        Optional<RiskProfile> profileOpt = profileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            if (isNightHours) {
                return new TimeResult(true, nightHoursBoost);
            }
            return new TimeResult(false, 0.0);
        }

        List<Integer> typicalHours = profileOpt.get().getTypicalTransactionHours();

        if (typicalHours == null || typicalHours.isEmpty()) {
            // New profile history
            if (isNightHours) {
                return new TimeResult(true, nightHoursBoost);
            }
            return new TimeResult(false, 0.0);
        }

        if (typicalHours.contains(hour)) {
            // Normal hour
            return new TimeResult(false, 0.0);
        } else {
            isUnusual = true;
            contribution += 0.05; // slight risk increase for hour not in profile
            if (isNightHours) {
                contribution += nightHoursBoost;
            }
            log.info("Unusual hour detected for user: {} (hour: {}), assigning boost: {}", userId, hour, contribution);
            return new TimeResult(isUnusual, contribution);
        }
    }

    public AmountResult analyzeAmountPattern(BigDecimal amount, String userId) {
        if (amount == null) {
            return new AmountResult(false, 0.0);
        }

        Optional<RiskProfile> profileOpt = profileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            return new AmountResult(false, 0.0);
        }

        RiskProfile profile = profileOpt.get();
        if (profile.getTotalTransactions() == 0 || profile.getAvgTransactionAmount() == null) {
            return new AmountResult(false, 0.0);
        }

        BigDecimal avgAmount = profile.getAvgTransactionAmount();
        BigDecimal threshold = avgAmount.multiply(BigDecimal.valueOf(3));

        if (amount.compareTo(threshold) > 0) {
            log.info("Amount anomaly detected for user: {} (amount: {} > 3x average: {}), assigning boost: 0.2", 
                    userId, amount, avgAmount);
            return new AmountResult(true, 0.2);
        }

        return new AmountResult(false, 0.0);
    }
}
