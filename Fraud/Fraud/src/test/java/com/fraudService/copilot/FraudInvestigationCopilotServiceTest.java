package com.fraudService.copilot;

import com.fraudService.copilot.model.InvestigationQueryRequest;
import com.fraudService.copilot.model.InvestigationSummaryResponse;
import com.fraudService.copilot.service.FraudInvestigationCopilotService;
import com.fraudService.copilot.tools.FraudInvestigationTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudInvestigationCopilotServiceTest {

    @Mock
    private FraudInvestigationTools tools;

    private FraudInvestigationCopilotService copilotService;

    @BeforeEach
    void setUp() {
        copilotService = new FraudInvestigationCopilotService(tools);
    }

    @Test
    @DisplayName("Valid Transaction Query retrieves telemetry and synthesizes complete 7-section report")
    void testInvestigateTransactionSuccess() {
        String txId = "TX9281";
        Long customerId = 101L;

        Map<String, Object> mockTx = new LinkedHashMap<>();
        mockTx.put("transactionId", txId);
        mockTx.put("customerId", customerId);
        mockTx.put("amount", new BigDecimal("75000.00"));
        mockTx.put("receiverUpiId", "merchant88@upi");
        mockTx.put("paymentMode", "UPI_COLLECT");
        mockTx.put("status", "SUCCESS");
        mockTx.put("deviceId", "DEV-IPHONE-9821");
        mockTx.put("ipAddress", "192.168.1.105");

        Map<String, Object> mockCust = new LinkedHashMap<>();
        mockCust.put("customerId", customerId);
        mockCust.put("fullName", "User 101");
        mockCust.put("accountAgeDays", 14);
        mockCust.put("kycStatus", "VERIFIED");

        Map<String, Object> mockRisk = new LinkedHashMap<>();
        mockRisk.put("riskScore", 0.82);
        mockRisk.put("riskLevel", "HIGH_RISK");
        mockRisk.put("decisionTier", "REQUIRES INVESTIGATION");

        when(tools.getTransaction(anyString())).thenReturn(mockTx);
        when(tools.getCustomer(customerId)).thenReturn(mockCust);
        when(tools.getRiskScore(anyString())).thenReturn(mockRisk);
        when(tools.getRiskReasons(anyString())).thenReturn(List.of("New Device +18", "Velocity +21"));
        when(tools.getCustomerTimeline(customerId)).thenReturn(List.of("12:00 Login", "12:30 Large Tx"));
        when(tools.getFraudNetwork(anyString())).thenReturn(Map.of("sharedDevices", List.of("DEV-IPHONE-9821")));
        when(tools.getPreviousCases(customerId)).thenReturn(List.of(Map.of("caseId", "CASE-101", "fraudType", "ATO", "status", "CONFIRMED_FRAUD", "resolution", "FRAUD")));

        InvestigationQueryRequest request = InvestigationQueryRequest.builder()
                .query("Investigate transaction TX9281")
                .build();

        InvestigationSummaryResponse response = copilotService.investigate(request);

        assertNotNull(response);
        assertTrue(response.isEvidenceFound(), "Evidence must be marked as found");
        assertFalse(response.getObservedEvidence().isEmpty(), "Observed Evidence section must be present");
        assertFalse(response.getRiskSignals().isEmpty(), "Risk Signals section must be present");
        assertFalse(response.getTransactionTimeline().isEmpty(), "Transaction Timeline section must be present");
        assertFalse(response.getRelatedEntities().isEmpty(), "Related Entities section must be present");
        assertFalse(response.getPreviousCases().isEmpty(), "Previous Cases section must be present");
        assertNotNull(response.getAssessment(), "Assessment section must be present");
        assertNotNull(response.getRecommendedNextStep(), "Recommended Next Step section must be present");
    }

    @Test
    @DisplayName("Anti-Hallucination Guardrail: Missing entity returns 'Insufficient evidence available.'")
    void testAntiHallucinationMissingEntity() {
        when(tools.getTransaction(anyString())).thenReturn(null);
        when(tools.getCustomer(anyLong())).thenReturn(null);

        InvestigationQueryRequest request = InvestigationQueryRequest.builder()
                .query("Investigate transaction INVALID_9999")
                .entityId("INVALID_9999")
                .build();

        InvestigationSummaryResponse response = copilotService.investigate(request);

        assertNotNull(response);
        assertFalse(response.isEvidenceFound(), "Evidence should be false for missing entity");
        assertEquals("Insufficient evidence available.", response.getAssessment());
        assertEquals("Insufficient evidence available.", response.getFormattedSummary());
    }

    @Test
    @DisplayName("Direct Tool Execution Validation: All 7 Spring AI tools return expected data structures")
    void testToolExecution() {
        FraudInvestigationTools realTools = new FraudInvestigationTools(null, null, null);

        Map<String, Object> tx = realTools.getTransaction("TX9281");
        assertNotNull(tx);
        assertEquals("TX9281", tx.get("transactionId"));

        Map<String, Object> cust = realTools.getCustomer(101L);
        assertNotNull(cust);
        assertEquals(101L, cust.get("customerId"));

        Map<String, Object> risk = realTools.getRiskScore("TX9281");
        assertNotNull(risk);
        assertEquals(0.82, risk.get("riskScore"));

        List<String> reasons = realTools.getRiskReasons("TX9281");
        assertFalse(reasons.isEmpty());

        List<String> timeline = realTools.getCustomerTimeline(101L);
        assertFalse(timeline.isEmpty());

        Map<String, Object> network = realTools.getFraudNetwork("TX9281");
        assertNotNull(network);

        List<Map<String, Object>> prevCases = realTools.getPreviousCases(101L);
        assertFalse(prevCases.isEmpty());
    }
}
