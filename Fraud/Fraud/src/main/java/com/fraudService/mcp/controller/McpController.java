package com.fraudService.mcp.controller;

import com.fraudService.mcp.audit.McpAuditLog;
import com.fraudService.mcp.audit.McpAuditLogger;
import com.fraudService.mcp.model.McpToolDefinition;
import com.fraudService.mcp.model.McpToolExecutionRequest;
import com.fraudService.mcp.model.McpToolExecutionResponse;
import com.fraudService.mcp.service.McpToolRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fraud/mcp")
@RequiredArgsConstructor
@Slf4j
public class McpController {

    private final McpToolRegistryService registryService;
    private final McpAuditLogger auditLogger;

    @GetMapping("/tools")
    public ResponseEntity<List<McpToolDefinition>> getTools() {
        log.info("REST GET /fraud/mcp/tools");
        List<McpToolDefinition> tools = registryService.getRegisteredTools();
        return ResponseEntity.ok(tools);
    }

    @PostMapping("/execute")
    public ResponseEntity<McpToolExecutionResponse> executeTool(@RequestBody McpToolExecutionRequest request) {
        log.info("REST POST /fraud/mcp/execute | toolName='{}' | confirmedByHuman={}", request.getToolName(), request.isConfirmedByHuman());
        McpToolExecutionResponse response = registryService.executeTool(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/audit")
    public ResponseEntity<List<McpAuditLog>> getAuditLogs() {
        log.info("REST GET /fraud/mcp/audit");
        List<McpAuditLog> logs = auditLogger.getAuditLogs();
        return ResponseEntity.ok(logs);
    }
}
