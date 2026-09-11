package com.fraudService.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudIntelligenceResult;
import com.fraudService.dto.response.FraudRiskLevel;
import com.fraudService.repository.FraudLogRepository;
import com.fraudService.rules.FraudRule;
import com.fraudService.rules.advanced.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudIntelligenceServiceTest {

    @Mock
    private FraudLogRepository fraudLogRepository;

    @Mock
    private FraudScoringService aiScoringService;

    private FraudIntelligenceService intelligenceService;

    @BeforeEach
    void setUp() {
        List<FraudRule> baselineRules = Collections.emptyList();
        List<AdvancedFraudRule> advancedRules = List.of(
                new AccountTakeoverRule(),
                new CardTestingRule(),
                new MoneyMuleRule(),
                new RapidFundMovementRule(),
                new BeneficiaryAbuseRule(),
                new IdentityAnomalyRule(),
                new TransactionVelocityAbuseRule(),
                new CoordinatedActivityRule()
        );

        ObjectMapper objectMapper = new ObjectMapper();

        intelligenceService = new FraudIntelligenceService(
                baselineRules,
                advancedRules,
                aiScoringService,
                fraudLogRepository,
                objectMapper
        );
    }

    @Test
    void testCleanPaymentReturnsLowRisk() {
        FraudCheckRequest req = FraudCheckRequest.builder()
                .paymentId("PAY1001")
                .senderId(101L)
                .receiverId(202L)
                .senderUpiId("alice@upimesh")
                .receiverUpiId("bob@upimesh")
                .amount(new BigDecimal("500.00"))
                .paymentMode("UPI")
                .deviceId("DEV_KNOWN_1")
                .ipAddress("127.0.0.1")
                .build();

        FraudIntelligenceResult result = intelligenceService.evaluateFraudIntelligence(req);

        assertNotNull(result);
        assertEquals(FraudRiskLevel.LOW_RISK, result.getRiskLevel());
        assertEquals("LOW RISK", result.getDecision());
        assertTrue(result.isAllowed());
        assertEquals(0.0, result.getRiskScore());
        verify(fraudLogRepository, times(1)).save(any());
    }

    @Test
    void testCardTestingMicroAmountDetected() {
        FraudCheckRequest req = FraudCheckRequest.builder()
                .paymentId("PAY1002")
                .senderId(101L)
                .receiverId(202L)
                .senderUpiId("alice@upimesh")
                .receiverUpiId("bob@upimesh")
                .amount(new BigDecimal("5.00"))
                .paymentMode("UPI")
                .deviceId("DEV_KNOWN_1")
                .ipAddress("127.0.0.1")
                .build();

        FraudIntelligenceResult result = intelligenceService.evaluateFraudIntelligence(req);

        assertNotNull(result);
        assertEquals(FraudRiskLevel.SUSPICIOUS, result.getRiskLevel());
        assertEquals("SUSPICIOUS", result.getDecision());
        assertTrue(result.isAllowed());
        assertTrue(result.getRiskScore() >= 0.25);
    }

    @Test
    void testAccountTakeoverAndMoneyMuleTriggersInvestigation() {
        FraudCheckRequest req = FraudCheckRequest.builder()
                .paymentId("PAY_BURST_1003")
                .senderId(101L)
                .receiverId(202L)
                .senderUpiId("alice@upimesh")
                .receiverUpiId("mule_account@upimesh")
                .amount(new BigDecimal("50000.00"))
                .paymentMode("UPI")
                .deviceId("DEV_NEW_ATO")
                .ipAddress("10.99.1.255")
                .build();

        when(aiScoringService.calculateFraudScore(any())).thenReturn(0.85);

        FraudIntelligenceResult result = intelligenceService.evaluateFraudIntelligence(req);

        assertNotNull(result);
        assertEquals(FraudRiskLevel.REQUIRES_INVESTIGATION, result.getRiskLevel());
        assertEquals("REQUIRES INVESTIGATION", result.getDecision());
        assertFalse(result.isAllowed());
        assertTrue(result.getRiskScore() >= 0.80);
    }
}
