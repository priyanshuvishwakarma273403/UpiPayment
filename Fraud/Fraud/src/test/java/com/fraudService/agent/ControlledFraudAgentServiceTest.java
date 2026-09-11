package com.fraudService.agent;

import com.fraudService.agent.model.AgentReportResponse;
import com.fraudService.agent.service.ControlledFraudAgentService;
import com.fraudService.mcp.tools.McpReadTools;
import com.fraudService.rag.model.PolicyQueryResult;
import com.fraudService.rag.service.FraudPolicyRagService;
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
class ControlledFraudAgentServiceTest {

    @Mock
    private McpReadTools readTools;

    @Mock
    private FraudPolicyRagService ragService;

    private ControlledFraudAgentService agentService;

    @BeforeEach
    void setUp() {
        agentService = new ControlledFraudAgentService(readTools, ragService);
    }

    @Test
    @DisplayName("Full 9-step customer investigation generates complete 8-section report")
    void testControlledInvestigationPipelineSuccess() {
        Long customerId = 9281L;

        Map<String, Object> mockCust = Map.of("customerId", customerId, "fullName", "User C9281", "accountAgeDays", 14, "kycStatus", "VERIFIED");
        Map<String, Object> mockTx = Map.of("transactionId", "TX9281", "amount", new BigDecimal("75000.00"), "receiverUpiId", "merchant88@upi", "paymentMode", "UPI_COLLECT", "status", "SUCCESS", "deviceId", "DEV-IPHONE-9821", "ipAddress", "192.168.1.105");
        Map<String, Object> mockRisk = Map.of("riskScore", 0.85, "riskLevel", "HIGH_RISK");
        Map<String, Object> mockGraph = Map.of("sharedDevices", List.of("DEV-IPHONE-9821"));

        when(readTools.getCustomer(anyLong())).thenReturn(mockCust);
        when(readTools.getTransaction(anyString())).thenReturn(mockTx);
        when(readTools.getRiskScore(anyString())).thenReturn(mockRisk);
        when(readTools.getFraudNetwork(anyString())).thenReturn(mockGraph);
        when(readTools.searchCases(anyString(), any(), any())).thenReturn(List.of(Map.of("caseId", "CASE-9281", "status", "OPEN")));
        when(ragService.searchPolicy(anyString(), anyInt())).thenReturn(List.of(PolicyQueryResult.builder().documentTitle("Account Takeover Policy").sectionHeader("Section 2").snippet("Apply hold").similarityScore(1.0).build()));

        String query = "Investigate customer C9281 for possible account takeover.";
        AgentReportResponse response = agentService.runControlledInvestigation(query);

        assertNotNull(response);
        assertEquals("C" + customerId, response.getTargetEntity());
        assertFalse(response.getEvidence().isEmpty(), "Section 1: Evidence must be present");
        assertFalse(response.getTimeline().isEmpty(), "Section 2: Timeline must be present");
        assertFalse(response.getNetwork().isEmpty(), "Section 3: Network must be present");
        assertFalse(response.getRisk().isEmpty(), "Section 4: Risk must be present");
        assertEquals("ACCOUNT_TAKEOVER (ATO)", response.getPotentialFraudType(), "Section 5: Potential Fraud Type");
        assertEquals("HIGH (92%)", response.getConfidence(), "Section 6: Confidence");
        assertNotNull(response.getMissingEvidence(), "Section 7: Missing Evidence");
        assertNotNull(response.getRecommendedAction(), "Section 8: Recommended Action");
        assertTrue(response.getToolCallsUsed() <= 15, "Tool call count must be bounded <= 15");
    }

    @Test
    @DisplayName("Bounded execution: Tool call limit is strictly respected")
    void testBoundedToolCallLimitEnforcement() {
        String query = "Investigate customer C9281 for possible account takeover.";
        AgentReportResponse response = agentService.runControlledInvestigation(query);

        assertTrue(response.getToolCallsUsed() <= 15, "Agent tool calls must not exceed MAX_TOOL_CALLS (15)");
    }

    @Test
    @DisplayName("Missing customer profile or transaction records entry under Missing Evidence")
    void testMissingEvidenceRecording() {
        when(readTools.getCustomer(anyLong())).thenReturn(null);
        when(readTools.getTransaction(anyString())).thenReturn(null);

        String query = "Investigate customer C9999 for possible account takeover.";
        AgentReportResponse response = agentService.runControlledInvestigation(query);

        assertNotNull(response);
        assertFalse(response.getMissingEvidence().isEmpty(), "Missing evidence must record missing profile entries");
    }

    @Test
    @DisplayName("Non-Destructive Action Rule: Agent recommends action without executing destructive actions")
    void testNonDestructiveActionRecommendation() {
        String query = "Investigate customer C9281 for possible account takeover.";
        AgentReportResponse response = agentService.runControlledInvestigation(query);

        assertNotNull(response.getRecommendedAction());
        assertTrue(response.getRecommendedAction().contains("FREEZE") || response.getRecommendedAction().contains("ESCALATE"));
        assertTrue(response.getFormattedReport().contains("Analyst"));
    }
}
