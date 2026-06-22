package com.upimesh.aml.service;

import com.upimesh.aml.exception.AlertNotFoundException;
import com.upimesh.aml.model.dto.StructuringResult;
import com.upimesh.aml.model.dto.VelocityResult;
import com.upimesh.aml.model.dto.WatchlistResult;
import com.upimesh.aml.model.entity.AmlAlert;
import com.upimesh.aml.model.entity.AmlScreeningResult;
import com.upimesh.aml.model.entity.WatchlistEntry;
import com.upimesh.aml.model.enums.AlertStatus;
import com.upimesh.aml.model.enums.AmlRiskLevel;
import com.upimesh.aml.model.enums.AlertType;
import com.upimesh.aml.model.request.AmlCheckRequest;
import com.upimesh.aml.model.response.AmlCheckResponse;
import com.upimesh.aml.repository.AmlAlertRepository;
import com.upimesh.aml.repository.AmlScreeningResultRepository;
import com.upimesh.aml.repository.WatchlistRepository;
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
public class AmlService {

    private final AmlScreeningResultRepository screeningRepository;
    private final AmlAlertRepository alertRepository;
    private final WatchlistRepository watchlistRepository;

    private final VelocityCheckService velocityCheckService;
    private final StructuringDetectionService structuringDetectionService;
    private final WatchlistScreeningService watchlistScreeningService;

    @Value("${aml.high-value-threshold:50000}")
    private BigDecimal highValueThreshold;

    @Transactional
    public AmlCheckResponse screenTransaction(AmlCheckRequest request) {
        String screeningId = UUID.randomUUID().toString();
        log.info("Screening transaction: {} for user: {}, receiver: {}, amount: {}", 
                request.getTransactionId(), request.getUserUpiId(), request.getReceiverUpiId(), request.getAmount());

        List<String> checksPerformed = List.of("VELOCITY_CHECK", "STRUCTURING_CHECK", "SENDER_WATCHLIST_CHECK", "RECEIVER_WATCHLIST_CHECK", "HIGH_VALUE_CHECK");
        List<String> flagsTriggered = new ArrayList<>();
        double riskScore = 0.0;
        String blockReason = null;

        // 1. Velocity check
        VelocityResult velocityResult = velocityCheckService.checkVelocity(request.getUserUpiId(), request.getAmount());
        if (velocityResult.exceeded()) {
            riskScore += 0.3;
            flagsTriggered.add("VELOCITY_BREACH");
            log.warn("Velocity breach flagged for user: {}. Reason: {}", request.getUserUpiId(), velocityResult.reason());
        }

        // 2. Structuring check
        StructuringResult structuringResult = structuringDetectionService.detectStructuring(request.getUserUpiId(), request.getAmount());
        if (structuringResult.detected()) {
            riskScore += 0.5;
            flagsTriggered.add("STRUCTURING_DETECTED");
            log.warn("Structuring detected for user: {}. Pattern: {}", request.getUserUpiId(), structuringResult.pattern());
        }

        // 3. Sender Watchlist check
        String senderName = extractNameFromUpiId(request.getUserUpiId());
        WatchlistResult senderWatchlistResult = watchlistScreeningService.screenName(senderName);
        boolean watchlistMatched = false;
        String matchedType = null;
        if (senderWatchlistResult.matched()) {
            riskScore += 0.8;
            watchlistMatched = true;
            matchedType = senderWatchlistResult.matchType();
            flagsTriggered.add("SENDER_WATCHLIST_MATCH_" + senderWatchlistResult.matchType());
            log.warn("Sender matched watchlist: {}. Type: {}, Source: {}", 
                    senderName, senderWatchlistResult.matchType(), senderWatchlistResult.source());
        }

        // 4. Receiver Watchlist check
        String receiverName = extractNameFromUpiId(request.getReceiverUpiId());
        WatchlistResult receiverWatchlistResult = watchlistScreeningService.screenName(receiverName);
        if (receiverWatchlistResult.matched()) {
            riskScore += 0.8;
            watchlistMatched = true;
            matchedType = receiverWatchlistResult.matchType();
            flagsTriggered.add("RECEIVER_WATCHLIST_MATCH_" + receiverWatchlistResult.matchType());
            log.warn("Receiver matched watchlist: {}. Type: {}, Source: {}", 
                    receiverName, receiverWatchlistResult.matchType(), receiverWatchlistResult.source());
        }

        // 5. High value check
        boolean highValue = request.getAmount().compareTo(highValueThreshold) >= 0;
        if (highValue) {
            riskScore += 0.2;
            flagsTriggered.add("HIGH_VALUE_ALERT");
            log.debug("High value transaction detected: {} >= {}", request.getAmount(), highValueThreshold);
        }

        // Cap risk score at 1.0
        if (riskScore > 1.0) {
            riskScore = 1.0;
        }

        // Determine Risk Level
        AmlRiskLevel riskLevel;
        if (riskScore < 0.3) {
            riskLevel = AmlRiskLevel.LOW;
        } else if (riskScore < 0.6) {
            riskLevel = AmlRiskLevel.MEDIUM;
        } else if (riskScore < 0.8) {
            riskLevel = AmlRiskLevel.HIGH;
        } else {
            riskLevel = AmlRiskLevel.BLOCKED;
        }

        boolean blocked = riskLevel == AmlRiskLevel.BLOCKED;
        if (blocked) {
            blockReason = "Transaction blocked due to critical compliance risk level (AML Score: " + riskScore + ")";
            if (watchlistMatched) {
                blockReason += " - Name matched PEP/Sanctions watchlists";
            } else if (velocityResult.exceeded() && structuringResult.detected()) {
                blockReason += " - Combination of structuring and velocity breach";
            }
        }

        // Save Screening Result
        AmlScreeningResult screeningResult = AmlScreeningResult.builder()
                .screeningId(screeningId)
                .transactionId(request.getTransactionId())
                .userUpiId(request.getUserUpiId())
                .amount(request.getAmount())
                .riskLevel(riskLevel)
                .riskScore(riskScore)
                .blocked(blocked)
                .blockReason(blockReason)
                .checksPerformed(checksPerformed)
                .build();
        screeningRepository.save(screeningResult);

        // Save Alerts if risk is MEDIUM, HIGH or BLOCKED
        if (riskLevel != AmlRiskLevel.LOW) {
            AlertType alertType = AlertType.UNUSUAL_PATTERN;
            String description = "Potential money laundering risk flagged. Triggered: " + String.join(", ", flagsTriggered);

            if (watchlistMatched) {
                alertType = "SANCTIONS".equalsIgnoreCase(matchedType) ? AlertType.SANCTIONS_MATCH : AlertType.PEP_MATCH;
            } else if (structuringResult.detected()) {
                alertType = AlertType.STRUCTURING;
                description = "Structuring alert: " + structuringResult.pattern();
            } else if (velocityResult.exceeded()) {
                alertType = AlertType.VELOCITY_BREACH;
                description = "Velocity limit breached: " + velocityResult.reason();
            } else if (highValue) {
                alertType = AlertType.HIGH_VALUE;
                description = String.format("High value transaction ₹%s meets or exceeds ₹%s reporting limit", 
                        request.getAmount().toPlainString(), highValueThreshold.toPlainString());
            }

            AmlAlert alert = AmlAlert.builder()
                    .alertId(UUID.randomUUID().toString())
                    .transactionId(request.getTransactionId())
                    .userUpiId(request.getUserUpiId())
                    .alertType(alertType)
                    .status(AlertStatus.OPEN)
                    .description(description)
                    .amount(request.getAmount())
                    .riskScore(riskScore)
                    .build();
            alertRepository.save(alert);
        }

        return AmlCheckResponse.builder()
                .transactionId(request.getTransactionId())
                .screeningId(screeningId)
                .riskLevel(riskLevel)
                .riskScore(riskScore)
                .allowed(!blocked)
                .blockReason(blockReason)
                .flagsTriggered(flagsTriggered)
                .screenedAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public AmlAlert resolveAlert(String alertId, String notes, String resolvedBy) {
        AmlAlert alert = alertRepository.findByAlertId(alertId)
                .orElseThrow(() -> new AlertNotFoundException("Alert not found with ID: " + alertId));

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolutionNotes(notes);
        alert.setAssignedTo(resolvedBy);
        alert.setResolvedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    @Transactional
    public AmlAlert escalateAlert(String alertId, String assignedTo) {
        AmlAlert alert = alertRepository.findByAlertId(alertId)
                .orElseThrow(() -> new AlertNotFoundException("Alert not found with ID: " + alertId));

        alert.setStatus(AlertStatus.ESCALATED);
        alert.setAssignedTo(assignedTo);
        alert.setEscalatedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    @Transactional
    public WatchlistEntry addToWatchlist(WatchlistEntry entry) {
        if (entry.getEntityId() == null) {
            entry.setEntityId(UUID.randomUUID().toString());
        }
        return watchlistRepository.save(entry);
    }

    public List<AmlAlert> getAlerts(String userUpiId) {
        return alertRepository.findByUserUpiIdOrderByCreatedAtDesc(userUpiId);
    }

    private String extractNameFromUpiId(String upiId) {
        if (upiId == null) {
            return "";
        }
        String localpart = upiId.split("@")[0];
        return localpart.replaceAll("[._-]", " ").trim();
    }
}
