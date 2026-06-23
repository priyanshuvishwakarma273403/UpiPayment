package com.upimesh.risk.service;

import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.request.RiskScoringRequest;
import com.upimesh.risk.repository.RiskProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskProfileUpdater {

    private final RiskProfileRepository profileRepository;
    private final DeviceFingerprintService deviceFingerprintService;

    @Async
    @Transactional
    public void updateRiskProfile(String userId, RiskScoringRequest request, boolean transactionSuccess) {
        log.info("Asynchronously updating risk profile for user: {}, success: {}", userId, transactionSuccess);

        RiskProfile profile = profileRepository.findByUserId(userId)
                .orElse(null);

        if (profile == null) {
            profile = RiskProfile.builder()
                    .userId(userId)
                    .userUpiId(request.getUserUpiId())
                    .baseRiskScore(0.1)
                    .totalTransactions(0)
                    .successfulTransactions(0)
                    .failedTransactions(0)
                    .knownDevices(new LinkedHashSet<>())
                    .usualCities(new LinkedHashSet<>())
                    .typicalTransactionHours(new ArrayList<>())
                    .build();
        }

        // 1. Device update (max 10, evict oldest)
        String hashedDevice = deviceFingerprintService.hashDeviceId(request.getDeviceId());
        Set<String> knownDevices = profile.getKnownDevices();
        if (knownDevices == null) {
            knownDevices = new LinkedHashSet<>();
        }
        // Ensure insertion order retention by using LinkedHashSet
        if (!(knownDevices instanceof LinkedHashSet)) {
            knownDevices = new LinkedHashSet<>(knownDevices);
        }
        knownDevices.add(hashedDevice);
        if (knownDevices.size() > 10) {
            String oldest = knownDevices.iterator().next();
            knownDevices.remove(oldest);
        }
        profile.setKnownDevices(knownDevices);

        // 2. City update (max 5, evict oldest)
        Set<String> usualCities = profile.getUsualCities();
        if (usualCities == null) {
            usualCities = new LinkedHashSet<>();
        }
        if (!(usualCities instanceof LinkedHashSet)) {
            usualCities = new LinkedHashSet<>(usualCities);
        }
        usualCities.add(request.getCity());
        if (usualCities.size() > 5) {
            String oldest = usualCities.iterator().next();
            usualCities.remove(oldest);
        }
        profile.setUsualCities(usualCities);

        // 3. Hour update
        int hour = LocalDateTime.now().getHour();
        List<Integer> typicalHours = profile.getTypicalTransactionHours();
        if (typicalHours == null) {
            typicalHours = new ArrayList<>();
        }
        if (!typicalHours.contains(hour)) {
            typicalHours.add(hour);
        }
        profile.setTypicalTransactionHours(typicalHours);

        // 4. Counter increments
        profile.setTotalTransactions(profile.getTotalTransactions() + 1);
        if (transactionSuccess) {
            profile.setSuccessfulTransactions(profile.getSuccessfulTransactions() + 1);
        } else {
            profile.setFailedTransactions(profile.getFailedTransactions() + 1);
        }

        // 5. Amount updates
        BigDecimal newAmount = request.getAmount();
        
        // Max amount update
        BigDecimal currentMax = profile.getMaxTransactionAmount();
        if (currentMax == null || newAmount.compareTo(currentMax) > 0) {
            profile.setMaxTransactionAmount(newAmount);
        }

        // Rolling average amount update
        BigDecimal currentAvg = profile.getAvgTransactionAmount();
        if (currentAvg == null || profile.getTotalTransactions() == 1) {
            profile.setAvgTransactionAmount(newAmount);
        } else {
            // New average = ((avg * (total - 1)) + newAmount) / total
            int total = profile.getTotalTransactions();
            BigDecimal sumBefore = currentAvg.multiply(BigDecimal.valueOf(total - 1));
            BigDecimal newAvg = sumBefore.add(newAmount)
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
            profile.setAvgTransactionAmount(newAvg);
        }

        // 6. Timestamps
        profile.setLastTransactionAt(LocalDateTime.now());
        profile.setProfileUpdatedAt(LocalDateTime.now());

        profileRepository.save(profile);
        log.info("Risk profile updated successfully for user: {}", userId);
    }
}
