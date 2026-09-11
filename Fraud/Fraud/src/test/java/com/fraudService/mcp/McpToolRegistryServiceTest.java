package com.fraudService.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudService.mcp.audit.McpAuditLog;
import com.fraudService.mcp.audit.McpAuditLogger;
import com.fraudService.mcp.model.McpToolExecutionRequest;
import com.fraudService.mcp.model.McpToolExecutionResponse;
import com.fraudService.mcp.service.McpToolRegistryService;
import com.fraudService.mcp.tools.McpReadTools;
import com.fraudService.mcp.tools.McpSensitiveTools;
import com.fraudService.mcp.tools.McpWriteTools;
import com.fraudService.repository.FraudCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class McpToolRegistryServiceTest {

    @Mock
    private FraudCaseRepository caseRepository;

    private McpAuditLogger auditLogger;
    private McpReadTools readTools;
    private McpWriteTools writeTools;
    private McpSensitiveTools sensitiveTools;
    private McpToolRegistryService registryService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        auditLogger = new McpAuditLogger(objectMapper);
        readTools = new McpReadTools(caseRepository);
        writeTools = new McpWriteTools(caseRepository);
        sensitiveTools = new McpSensitiveTools();

        registryService = new McpToolRegistryService(readTools, writeTools, sensitiveTools, auditLogger);
        registryService.registerTools();
    }

    @Test
    @DisplayName("Read Tools execute successfully without requiring human confirmation")
    void testReadToolsExecution() {
        McpToolExecutionRequest req = McpToolExecutionRequest.builder()
                .toolName("get_transaction")
                .arguments(Map.of("tx_id", "TX9281"))
                .actor("AI_INVESTIGATOR")
                .correlationId("CORR-1001")
                .build();

        McpToolExecutionResponse res = registryService.executeTool(req);

        assertNotNull(res);
        assertEquals("SUCCESS", res.getStatus());
        assertNotNull(res.getResult());
        assertEquals("CORR-1001", res.getCorrelationId());
    }

    @Test
    @DisplayName("Write Tools execute successfully and record audit entries")
    void testWriteToolsExecution() {
        McpToolExecutionRequest req = McpToolExecutionRequest.builder()
                .toolName("create_case")
                .arguments(Map.of("customer_id", 101L, "transaction_id", "TX9281", "fraud_type", "ATO"))
                .actor("AI_INVESTIGATOR")
                .correlationId("CORR-1002")
                .build();

        McpToolExecutionResponse res = registryService.executeTool(req);

        assertNotNull(res);
        assertEquals("SUCCESS", res.getStatus());

        List<McpAuditLog> auditLogs = auditLogger.getAuditLogs();
        assertFalse(auditLogs.isEmpty());
        assertEquals("create_case", auditLogs.get(auditLogs.size() - 1).getTool());
    }

    @Test
    @DisplayName("Human-in-the-Loop Guardrail: Sensitive tool without human confirmation returns REQUIRES_HUMAN_CONFIRMATION")
    void testSensitiveActionWithoutHumanConfirmationBlocked() {
        McpToolExecutionRequest req = McpToolExecutionRequest.builder()
                .toolName("freeze_account")
                .arguments(Map.of("customer_id", 101L, "reason", "Suspected ATO"))
                .actor("AI_INVESTIGATOR")
                .confirmedByHuman(false) // No human approval
                .correlationId("CORR-1003")
                .build();

        McpToolExecutionResponse res = registryService.executeTool(req);

        assertNotNull(res);
        assertEquals("REQUIRES_HUMAN_CONFIRMATION", res.getStatus());
        assertNotNull(res.getConfirmationPrompt());
        assertTrue(res.getConfirmationPrompt().contains("freeze_account"));
    }

    @Test
    @DisplayName("Sensitive tool with attached human analyst confirmation executes successfully")
    void testSensitiveActionWithHumanConfirmationSuccess() {
        McpToolExecutionRequest req = McpToolExecutionRequest.builder()
                .toolName("freeze_account")
                .arguments(Map.of("customer_id", 101L, "reason", "Confirmed ATO"))
                .actor("AI_INVESTIGATOR")
                .confirmedByHuman(true) // Human Analyst Approved
                .humanAnalystId("ANALYST_404")
                .correlationId("CORR-1004")
                .build();

        McpToolExecutionResponse res = registryService.executeTool(req);

        assertNotNull(res);
        assertEquals("SUCCESS", res.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> resultData = (Map<String, Object>) res.getResult();
        assertEquals("ACCOUNT_FROZEN", resultData.get("status"));
        assertEquals("ANALYST_404", resultData.get("humanAnalystId"));
    }

    @Test
    @DisplayName("Audit Logger scrubs secret fields from logged JSON arguments")
    void testAuditLoggerScrubsSecrets() {
        Map<String, Object> argsWithSecrets = Map.of(
                "customer_id", 101L,
                "password", "SecretPass123!",
                "api_key", "sk_live_998877",
                "reason", "Test Audit"
        );

        McpAuditLog logEntry = auditLogger.logExecution("TEST_ACTOR", "test_tool", argsWithSecrets, Collections.emptyMap(), "CORR-9999", "SUCCESS", true);

        assertNotNull(logEntry);
        assertTrue(logEntry.getArgumentsJson().contains("[REDACTED_SECRET]"));
        assertFalse(logEntry.getArgumentsJson().contains("SecretPass123!"));
        assertFalse(logEntry.getArgumentsJson().contains("sk_live_998877"));
    }
}
