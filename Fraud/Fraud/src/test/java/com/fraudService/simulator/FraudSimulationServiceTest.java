package com.fraudService.simulator;

import com.fraudService.rules.FraudRule;
import com.fraudService.rules.advanced.AccountTakeoverRule;
import com.fraudService.rules.advanced.AdvancedFraudRule;
import com.fraudService.rules.advanced.TransactionVelocityAbuseRule;
import com.fraudService.service.FraudScoringService;
import com.fraudService.simulator.model.FraudSimulationRequest;
import com.fraudService.simulator.model.FraudSimulationResponse;
import com.fraudService.simulator.service.FraudSimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FraudSimulationServiceTest {

    private FraudSimulationService simulationService;
    private FraudScoringService scoringServiceMock;

    @BeforeEach
    void setUp() {
        scoringServiceMock = Mockito.mock(FraudScoringService.class);
        List<FraudRule> baselineRules = List.of();
        List<AdvancedFraudRule> advancedRules = List.of(
                new AccountTakeoverRule(),
                new TransactionVelocityAbuseRule()
        );

        simulationService = new FraudSimulationService(baselineRules, advancedRules, scoringServiceMock);
    }

    @Test
    void testBenignTransactionSimulation() {
        when(scoringServiceMock.calculateFraudScore(any())).thenReturn(0.05);

        FraudSimulationRequest simReq = FraudSimulationRequest.builder()
                .transactionAmount(new BigDecimal("1200"))
                .accountAgeDays(365.0)
                .deviceAgeDays(180.0)
                .beneficiaryAgeDays(120.0)
                .velocity(1.0)
                .locationDeviation(2.0)
                .ipRiskScore(0.05)
                .merchantRiskScore(0.10)
                .simulatedCustomerId("CUS_SAFE_01")
                .build();

        FraudSimulationResponse response = simulationService.runSimulation(simReq);

        assertNotNull(response);
        assertTrue(response.isSimulationMode());
        assertEquals("SIMULATION MODE", response.getModeLabel());
        assertEquals("ALLOWED", response.getDecision());
        assertEquals("LOW_RISK", response.getRiskLevel());
        assertTrue(response.getFinalRisk() < 0.25);
    }

    @Test
    void testHighRiskAccountTakeoverAttackSimulation() {
        when(scoringServiceMock.calculateFraudScore(any())).thenReturn(0.85);

        FraudSimulationRequest simReq = FraudSimulationRequest.builder()
                .transactionAmount(new BigDecimal("85000"))
                .accountAgeDays(180.0)
                .deviceAgeDays(0.2)
                .beneficiaryAgeDays(30.0)
                .velocity(1.5)
                .locationDeviation(125.0)
                .ipRiskScore(0.85)
                .merchantRiskScore(0.30)
                .simulatedCustomerId("CUS_ATO_01")
                .build();

        FraudSimulationResponse response = simulationService.runSimulation(simReq);

        assertNotNull(response);
        assertTrue(response.isSimulationMode());
        assertEquals("SIMULATION MODE", response.getModeLabel());
        assertEquals("BLOCKED", response.getDecision());
        assertEquals("REQUIRES_INVESTIGATION", response.getRiskLevel());
        assertTrue(response.getFinalRisk() >= 0.80);
        assertFalse(response.getRulesTriggered().isEmpty());
    }

    @Test
    void testZeroDatabaseWritesAndSimulationModeLabel() {
        when(scoringServiceMock.calculateFraudScore(any())).thenReturn(0.50);

        FraudSimulationRequest simReq = FraudSimulationRequest.builder()
                .transactionAmount(new BigDecimal("35000"))
                .velocity(6.0)
                .simulatedCustomerId("CUS_SIM_TEST")
                .build();

        FraudSimulationResponse response = simulationService.runSimulation(simReq);

        assertNotNull(response);
        assertTrue(response.isSimulationMode());
        assertEquals("SIMULATION MODE", response.getModeLabel());
        assertTrue(response.getBehavioralRisk() > 0.0);
        assertTrue(response.getGraphRisk() >= 0.0);
        assertTrue(response.getMlProbability() > 0.0);
    }
}
