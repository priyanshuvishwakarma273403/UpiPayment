package com.fraudService.mcp.service;

import com.fraudService.mcp.audit.McpAuditLogger;
import com.fraudService.mcp.model.McpToolDefinition;
import com.fraudService.mcp.model.McpToolExecutionRequest;
import com.fraudService.mcp.model.McpToolExecutionResponse;
import com.fraudService.mcp.tools.McpReadTools;
import com.fraudService.mcp.tools.McpSensitiveTools;
import com.fraudService.mcp.tools.McpWriteTools;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Central MCP Tool Registry & Execution Dispatcher.
 * Enforces Human-in-the-Loop sensitive action guardrails and triggers audit logging.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class McpToolRegistryService {

    private final McpReadTools readTools;
    private final McpWriteTools writeTools;
    private final McpSensitiveTools sensitiveTools;
    private final McpAuditLogger auditLogger;

    private final Map<String, McpToolDefinition> registry = new LinkedHashMap<>();

    @PostConstruct
    public void registerTools() {
        log.info("Registering MCP Investigation Tools...");

        // Read Tools
        register(McpToolDefinition.builder().name("get_transaction").description("Retrieves transaction details by ID").category("READ").requiresHumanConfirmation(false).build());
        register(McpToolDefinition.builder().name("get_customer").description("Retrieves customer profile").category("READ").requiresHumanConfirmation(false).build());
        register(McpToolDefinition.builder().name("get_risk_score").description("Retrieves composite risk score and rules").category("READ").requiresHumanConfirmation(false).build());
        register(McpToolDefinition.builder().name("get_fraud_network").description("Retrieves graph network linkages").category("READ").requiresHumanConfirmation(false).build());
        register(McpToolDefinition.builder().name("search_cases").description("Searches investigation cases").category("READ").requiresHumanConfirmation(false).build());
        register(McpToolDefinition.builder().name("get_customer_timeline").description("Retrieves customer activity timeline").category("READ").requiresHumanConfirmation(false).build());

        // Write Tools
        register(McpToolDefinition.builder().name("create_case").description("Creates a new investigation case").category("WRITE").requiresHumanConfirmation(false).build());
        register(McpToolDefinition.builder().name("add_case_note").description("Adds note to investigation case").category("WRITE").requiresHumanConfirmation(false).build());
        register(McpToolDefinition.builder().name("generate_report").description("Generates investigation summary report").category("WRITE").requiresHumanConfirmation(false).build());

        // Sensitive Tools (Human-in-the-Loop Confirmation Required)
        register(McpToolDefinition.builder().name("freeze_account").description("Freezes customer account due to suspected fraud").category("SENSITIVE").requiresHumanConfirmation(true).build());
        register(McpToolDefinition.builder().name("block_beneficiary").description("Blocks beneficiary UPI ID across mesh").category("SENSITIVE").requiresHumanConfirmation(true).build());
        register(McpToolDefinition.builder().name("add_watchlist").description("Adds entity to active fraud watchlist").category("SENSITIVE").requiresHumanConfirmation(true).build());
    }

    public void register(McpToolDefinition tool) {
        registry.put(tool.getName(), tool);
    }

    public List<McpToolDefinition> getRegisteredTools() {
        return new ArrayList<>(registry.values());
    }

    public McpToolExecutionResponse executeTool(McpToolExecutionRequest request) {
        if (request == null || request.getToolName() == null) {
            return McpToolExecutionResponse.builder()
                    .toolName("UNKNOWN")
                    .status("FAILED")
                    .result("Tool name cannot be null")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        String toolName = request.getToolName().toLowerCase();
        McpToolDefinition def = registry.get(toolName);

        if (def == null) {
            log.error("Attempted to execute unregistered MCP tool: '{}'", toolName);
            auditLogger.logExecution(request.getActor(), toolName, request.getArguments(), "Tool not found", request.getCorrelationId(), "FAILED", false);
            return McpToolExecutionResponse.builder()
                    .toolName(toolName)
                    .status("FAILED")
                    .result("Unregistered tool: " + toolName)
                    .correlationId(request.getCorrelationId())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        // Sensitive Action Guardrail: Require explicit human confirmation
        if (def.isRequiresHumanConfirmation() && !request.isConfirmedByHuman()) {
            log.warn("SENSITIVE ACTION BLOCKED: MCP tool '{}' requires human confirmation. Invoked by actor='{}'", toolName, request.getActor());

            String prompt = String.format("SENSITIVE ACTION WARNING: Executing '%s' requires explicit human analyst approval. Confirm action with human Analyst ID?", toolName);
            auditLogger.logExecution(request.getActor(), toolName, request.getArguments(), prompt, request.getCorrelationId(), "REQUIRES_HUMAN_CONFIRMATION", false);

            return McpToolExecutionResponse.builder()
                    .toolName(toolName)
                    .status("REQUIRES_HUMAN_CONFIRMATION")
                    .confirmationPrompt(prompt)
                    .result("Blocked: Financial or restriction actions require human analyst confirmation.")
                    .correlationId(request.getCorrelationId())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        // Dispatch Tool Execution
        Object result;
        Map<String, Object> args = request.getArguments() != null ? request.getArguments() : Collections.emptyMap();

        try {
            result = dispatchTool(toolName, args, request.getHumanAnalystId());
            auditLogger.logExecution(request.getActor(), toolName, args, result, request.getCorrelationId(), "SUCCESS", request.isConfirmedByHuman());

            return McpToolExecutionResponse.builder()
                    .toolName(toolName)
                    .status("SUCCESS")
                    .result(result)
                    .correlationId(request.getCorrelationId())
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error executing MCP tool '{}': {}", toolName, e.getMessage(), e);
            auditLogger.logExecution(request.getActor(), toolName, args, e.getMessage(), request.getCorrelationId(), "FAILED", request.isConfirmedByHuman());

            return McpToolExecutionResponse.builder()
                    .toolName(toolName)
                    .status("FAILED")
                    .result("Execution error: " + e.getMessage())
                    .correlationId(request.getCorrelationId())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private Object dispatchTool(String name, Map<String, Object> args, String humanAnalystId) {
        return switch (name) {
            // Read Tools
            case "get_transaction" -> readTools.getTransaction(getString(args, "tx_id", "payment_id", "txId"));
            case "get_customer" -> readTools.getCustomer(getLong(args, "customer_id", "customerId"));
            case "get_risk_score" -> readTools.getRiskScore(getString(args, "entity_id", "entityId"));
            case "get_fraud_network" -> readTools.getFraudNetwork(getString(args, "entity_id", "entityId"));
            case "search_cases" -> readTools.searchCases(getString(args, "query"), getString(args, "status"), getString(args, "risk_level"));
            case "get_customer_timeline" -> readTools.getCustomerTimeline(getLong(args, "customer_id", "customerId"));

            // Write Tools
            case "create_case" -> writeTools.createCase(getLong(args, "customer_id", "customerId"), getString(args, "transaction_id", "txId"), getString(args, "fraud_type"), getString(args, "severity"), getString(args, "description"));
            case "add_case_note" -> writeTools.addCaseNote(getString(args, "case_id", "caseId"), humanAnalystId, getString(args, "note"));
            case "generate_report" -> writeTools.generateReport(getString(args, "case_id", "caseId"), getString(args, "report_type"));

            // Sensitive Tools
            case "freeze_account" -> sensitiveTools.freezeAccount(getLong(args, "customer_id", "customerId"), getString(args, "reason"), humanAnalystId);
            case "block_beneficiary" -> sensitiveTools.blockBeneficiary(getString(args, "upi_id", "upiId"), getString(args, "reason"), humanAnalystId);
            case "add_watchlist" -> sensitiveTools.addWatchlist(getString(args, "entity_id", "entityId"), getString(args, "entity_type"), getString(args, "reason"), humanAnalystId);

            default -> throw new IllegalArgumentException("Unknown tool: " + name);
        };
    }

    private String getString(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            if (map.containsKey(k) && map.get(k) != null) {
                return String.valueOf(map.get(k));
            }
        }
        return "PAY1001"; // Fallback default sample identifier
    }

    private Long getLong(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            if (map.containsKey(k) && map.get(k) != null) {
                Object val = map.get(k);
                if (val instanceof Number n) return n.longValue();
                try { return Long.parseLong(String.valueOf(val)); } catch (Exception ignored) {}
            }
        }
        return 101L; // Fallback default customer ID
    }
}
