package com.fraudService.explanation;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudIntelligenceResult;
import com.fraudService.dto.response.FraudRiskLevel;
import com.fraudService.dto.response.FraudSignal;
import com.fraudService.entity.FraudLog;
import com.fraudService.explanation.model.ExplainableFraudDecision;
import com.fraudService.explanation.service.FraudExplanationService;
import com.fraudService.network.model.NetworkGraph;
import com.fraudService.network.service.FraudNetworkIntelligenceService;
import com.fraudService.repository.FraudLogRepository;
import com.fraudService.service.FraudIntelligenceService;
import com.fraudService.service.FraudScoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudExplanationServiceTest {

    @Mock
    private FraudIntelligenceService intelligenceService;

    @Mock
    private FraudScoringService scoringService;

    @Mock
    private FraudNetworkIntelligenceService networkService;

    @Mock
    private FraudLogRepository fraudLogRepository;

    private FraudExplanationService explanationService;

    @BeforeEach
    void setUp() {
        explanationService = new FraudExplanationService(intelligenceService, scoringService, networkService, fraudLogRepository);
    }

    @Test
    void testExplainTransactionGeneratesEmpiricalReport() {
        FraudCheckRequest req = FraudCheckRequest.builder()
                .paymentId("PAY1003")
                .senderId(101L)
                .amount(new BigDecimal("50000.00"))
                .deviceId("DEV_NEW_1")
                .ipAddress("10.0.0.1")
                .receiverUpiId("mule@upimesh")
                .build();

        FraudSignal sig1 = FraudSignal.builder()
                .signalId("RULE_NEW_DEVICE")
                .signalName("New Device")
                .scoreContribution(0.18)
                .triggered(true)
                .explanation("Unrecognized hardware signature")
                .build();

        FraudIntelligenceResult intel = FraudIntelligenceResult.builder()
                .paymentId("PAY1003")
                .riskScore(0.85)
                .riskLevel(FraudRiskLevel.REQUIRES_INVESTIGATION)
                .decision("REQUIRES INVESTIGATION")
                .allowed(false)
                .fraudSignals(List.of(sig1))
                .build();

        NetworkGraph graph = NetworkGraph.builder()
                .nodes(Collections.emptyList())
                .relationships(Collections.emptyList())
                .clusterRiskScore(0.80)
                .build();

        when(intelligenceService.evaluateFraudIntelligence(any())).thenReturn(intel);
        when(scoringService.calculateFraudScore(any())).thenReturn(0.86);
        when(networkService.getCustomerNetwork(any(), anyInt())).thenReturn(graph);

        ExplainableFraudDecision decision = explanationService.explainTransaction(req);

        assertNotNull(decision);
        assertEquals("PAY1003", decision.getPaymentId());
        assertEquals("REQUIRES INVESTIGATION", decision.getOverallDecision());
        assertEquals(0.85, decision.getOverallRiskScore());

        // Verify Rule Evidence exact score contribution formatting (+18)
        assertFalse(decision.getRuleEvidence().isEmpty());
        assertEquals("+18", decision.getRuleEvidence().get(0).getScoreContributionFormatted());

        // Verify ML Evidence
        assertNotNull(decision.getMlEvidence());
        assertEquals(0.86, decision.getMlEvidence().getFraudProbability());

        // Verify Empirical Evidence enforcement (No generic text)
        assertFalse(decision.getRuleEvidence().get(0).getReason().contains("AI detected suspicious behavior"));
    }

    @Test
    void testExplainPaymentIdFetchesLog() {
        FraudLog logDoc = FraudLog.builder()
                .paymentId("PAY1001")
                .senderId(101L)
                .amount(new BigDecimal("500.00"))
                .deviceId("DEV_1")
                .ipAddress("127.0.0.1")
                .checkedAt(LocalDateTime.now())
                .build();

        FraudIntelligenceResult intel = FraudIntelligenceResult.builder()
                .paymentId("PAY1001")
                .riskScore(0.05)
                .riskLevel(FraudRiskLevel.LOW_RISK)
                .decision("LOW RISK")
                .allowed(true)
                .build();

        when(fraudLogRepository.findByPaymentId("PAY1001")).thenReturn(Optional.of(logDoc));
        when(intelligenceService.evaluateFraudIntelligence(any())).thenReturn(intel);
        when(scoringService.calculateFraudScore(any())).thenReturn(0.05);
        when(networkService.getCustomerNetwork(any(), anyInt())).thenReturn(NetworkGraph.builder().nodes(Collections.emptyList()).build());

        ExplainableFraudDecision decision = explanationService.explainPaymentId("PAY1001");

        assertNotNull(decision);
        assertEquals("PAY1001", decision.getPaymentId());
        assertEquals("LOW RISK", decision.getOverallDecision());
    }
}
