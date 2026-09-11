package com.upimesh.risk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upimesh.risk.config.RiskThresholdConfig;
import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.enums.RiskDecision;
import com.upimesh.risk.model.enums.RiskLevel;
import com.upimesh.risk.model.request.RiskScoringRequest;
import com.upimesh.risk.model.response.RiskScoringResponse;
import com.upimesh.risk.repository.RiskProfileRepository;
import com.upimesh.risk.repository.RiskScoringResultRepository;
import com.upimesh.risk.rule.RiskRule;
import com.upimesh.risk.rule.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskScoringServiceTest {

    @Mock
    private RiskProfileRepository profileRepository;

    @Mock
    private RiskScoringResultRepository scoringResultRepository;

    @Mock
    private RiskProfileUpdater riskProfileUpdater;

    @Mock
    private RedisVelocityTracker velocityTracker;

    private RiskScoringService riskScoringService;

    @BeforeEach
    void setUp() {
        RiskThresholdConfig config = new RiskThresholdConfig();
        config.setAllowThreshold(20.0);
        config.setMonitorThreshold(40.0);
        config.setStepUpThreshold(60.0);
        config.setReviewThreshold(80.0);

        lenient().when(velocityTracker.trackAndEvaluateVelocity(any(), any(), any(), any()))
                .thenReturn(com.upimesh.risk.model.dto.VelocityResult.builder()
                        .customerCount1m(1)
                        .customerCount5m(1)
                        .customerCount1h(1)
                        .deviceCount1m(1)
                        .deviceCount1h(1)
                        .ipCount1m(1)
                        .ipCount1h(1)
                        .beneficiaryCount1m(1)
                        .beneficiaryCount1h(1)
                        .degraded(false)
                        .build());

        List<RiskRule> rules = List.of(
                new AmountDeviationRule(),
                new TransactionFrequencyVelocityRule(velocityTracker),
                new NewDeviceRule(),
                new NewBeneficiaryRule(),
                new UnusualHoursRule(),
                new LocationDeviationRule(),
                new AccountAgeRule(),
                new MerchantRiskRule(),
                new PreviousFraudHistoryRule()
        );

        ObjectMapper objectMapper = new ObjectMapper();

        riskScoringService = new RiskScoringService(
                profileRepository,
                scoringResultRepository,
                rules,
                config,
                objectMapper,
                riskProfileUpdater
        );
    }

    @Test
    void testLowRiskTransactionAllowed() {
        RiskScoringRequest request = RiskScoringRequest.builder()
                .transactionId("TXN1001")
                .userId("user123")
                .userUpiId("user123@upimesh")
                .receiverUpiId("merchant@upimesh")
                .amount(new BigDecimal("500.00"))
                .deviceId("DEV_KNOWN_1")
                .ipAddress("127.0.0.1")
                .city("Mumbai")
                .transactionType("PAYMENT")
                .build();

        RiskProfile profile = RiskProfile.builder()
                .userId("user123")
                .userUpiId("user123@upimesh")
                .avgTransactionAmount(new BigDecimal("600.00"))
                .knownDevices(Set.of("DEV_KNOWN_1"))
                .usualCities(Set.of("Mumbai"))
                .baseRiskScore(0.1)
                .successfulTransactions(10)
                .build();

        when(profileRepository.findByUserId("user123")).thenReturn(Optional.of(profile));

        RiskScoringResponse response = riskScoringService.scoreTransaction(request);

        assertNotNull(response);
        assertEquals(RiskDecision.ALLOW, response.getDecision());
        assertEquals(RiskLevel.LOW, response.getRiskLevel());
        assertTrue(response.isAllowed());
        assertTrue(response.getFinalScore() < 20.0);
        verify(scoringResultRepository, times(1)).save(any());
    }

    @Test
    void testNewDeviceAndLocationDeviationTriggersMonitorOrStepUp() {
        RiskScoringRequest request = RiskScoringRequest.builder()
                .transactionId("TXN1002")
                .userId("user123")
                .userUpiId("user123@upimesh")
                .receiverUpiId("merchant@upimesh")
                .amount(new BigDecimal("500.00"))
                .deviceId("DEV_NEW_UNKNOWN")
                .ipAddress("127.0.0.1")
                .city("Delhi")
                .transactionType("PAYMENT")
                .build();

        RiskProfile profile = RiskProfile.builder()
                .userId("user123")
                .userUpiId("user123@upimesh")
                .avgTransactionAmount(new BigDecimal("600.00"))
                .knownDevices(Set.of("DEV_KNOWN_1"))
                .usualCities(Set.of("Mumbai"))
                .baseRiskScore(0.1)
                .successfulTransactions(10)
                .build();

        when(profileRepository.findByUserId("user123")).thenReturn(Optional.of(profile));

        RiskScoringResponse response = riskScoringService.scoreTransaction(request);

        assertNotNull(response);
        // New Device (+25) + Location Deviation (+20) = 45.0 => STEP_UP
        assertEquals(RiskDecision.STEP_UP, response.getDecision());
        assertEquals(RiskLevel.MEDIUM, response.getRiskLevel());
        assertFalse(response.isAllowed()); // allowed is only for ALLOW & MONITOR
    }

    @Test
    void testHighRiskMerchantAndFraudHistoryTriggersBlock() {
        RiskScoringRequest request = RiskScoringRequest.builder()
                .transactionId("TXN1003")
                .userId("user123")
                .userUpiId("user123@upimesh")
                .receiverUpiId("gambling_site@upimesh")
                .amount(new BigDecimal("50000.00"))
                .deviceId("DEV_NEW_UNKNOWN")
                .ipAddress("127.0.0.1")
                .city("Delhi")
                .merchantCategory("CASINO")
                .transactionType("PAYMENT")
                .build();

        RiskProfile profile = RiskProfile.builder()
                .userId("user123")
                .userUpiId("user123@upimesh")
                .avgTransactionAmount(new BigDecimal("100.00")) // 500x average (+35)
                .knownDevices(Set.of("DEV_KNOWN_1")) // New device (+25)
                .usualCities(Set.of("Mumbai")) // Location anomaly (+20)
                .baseRiskScore(0.8) // High base risk / fraud history (+40)
                .successfulTransactions(1)
                .build();

        when(profileRepository.findByUserId("user123")).thenReturn(Optional.of(profile));

        RiskScoringResponse response = riskScoringService.scoreTransaction(request);

        assertNotNull(response);
        assertEquals(RiskDecision.BLOCK, response.getDecision());
        assertEquals(RiskLevel.CRITICAL, response.getRiskLevel());
        assertFalse(response.isAllowed());
        assertEquals(100.0, response.getFinalScore());
        verify(riskProfileUpdater, times(1)).updateRiskProfile(eq("user123"), eq(request), eq(false));
    }
}
