package com.upimesh.aml.service;

import com.upimesh.aml.model.dto.StructuringResult;
import com.upimesh.aml.model.entity.AmlScreeningResult;
import com.upimesh.aml.repository.AmlScreeningResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StructuringDetectionService {

    private final AmlScreeningResultRepository screeningRepository;

    @Value("${aml.structuring-window-hours:24}")
    private int windowHours;

    @Value("${aml.structuring-threshold:90000.00}")
    private BigDecimal thresholdAmount;

    public StructuringResult detectStructuring(String userUpiId, BigDecimal currentAmount) {
        LocalDateTime since = LocalDateTime.now().minusHours(windowHours);
        List<AmlScreeningResult> recentTxns = screeningRepository.findByUserUpiIdAndScreenedAtAfter(userUpiId, since);

        BigDecimal total24h = currentAmount;
        int count = recentTxns.size() + 1;

        for (AmlScreeningResult res : recentTxns) {
            total24h = total24h.add(res.getAmount());
        }

        boolean thresholdBreached = total24h.compareTo(thresholdAmount) > 0 && count >= 3;

        // Check for "just under limit" pattern (e.g. ₹9,000 to ₹9,999, or ₹45,000 to ₹49,999)
        int justUnderLimitCount = 0;
        if (isJustUnderLimit(currentAmount)) {
            justUnderLimitCount++;
        }
        for (AmlScreeningResult res : recentTxns) {
            if (isJustUnderLimit(res.getAmount())) {
                justUnderLimitCount++;
            }
        }

        boolean patternBreached = justUnderLimitCount >= 3;

        if (thresholdBreached) {
            String pattern = String.format("Multiple transactions (%d) totaling %s exceeding threshold %s in last %dh",
                    count, total24h.toPlainString(), thresholdAmount.toPlainString(), windowHours);
            return new StructuringResult(true, total24h, count, pattern);
        }

        if (patternBreached) {
            String pattern = String.format("Detected multiple transactions (%d) in the last 24h just below the ₹10,000 or ₹50,000 reporting limits", 
                    justUnderLimitCount);
            return new StructuringResult(true, total24h, count, pattern);
        }

        return new StructuringResult(false, total24h, count, "No structuring patterns identified");
    }

    private boolean isJustUnderLimit(BigDecimal amount) {
        double val = amount.doubleValue();
        // Just under ₹10,000 (e.g., ₹9,000 to ₹9,999.99)
        boolean nearTenK = val >= 9000.0 && val <= 9999.99;
        // Just under ₹50,000 (e.g., ₹45,000 to ₹49,999.99)
        boolean nearFiftyK = val >= 45000.0 && val <= 49999.99;
        return nearTenK || nearFiftyK;
    }
}
