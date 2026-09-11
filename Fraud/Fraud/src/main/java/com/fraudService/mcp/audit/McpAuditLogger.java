package com.fraudService.mcp.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Immutable MCP Audit Logger.
 * Records all MCP tool invocations (actor, tool, timestamp, arguments, result, correlationId)
 * while strictly scrubbing secret fields (passwords, tokens, API keys, PINs).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class McpAuditLogger {

    private final ObjectMapper objectMapper;

    @Getter
    private final List<McpAuditLog> auditLogs = new CopyOnWriteArrayList<>();

    private static final Set<String> SECRET_FIELDS = Set.of(
            "password", "secret", "token", "apikey", "api_key", "auth_token", "pin", "cvv"
    );

    public McpAuditLog logExecution(String actor, String tool, Map<String, Object> arguments, Object result, String correlationId, String status, boolean humanConfirmed) {
        String sanitizedArgsJson = sanitizeAndSerialize(arguments);
        String sanitizedResultJson = sanitizeAndSerialize(result);

        McpAuditLog logEntry = McpAuditLog.builder()
                .auditId("AUD-MCP-" + UUID.randomUUID().toString().substring(0, 8))
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .actor(actor != null ? actor : "AI_AGENT")
                .tool(tool)
                .timestamp(LocalDateTime.now())
                .argumentsJson(sanitizedArgsJson)
                .resultJson(sanitizedResultJson)
                .status(status)
                .humanConfirmed(humanConfirmed)
                .build();

        auditLogs.add(logEntry);
        log.info("MCP AUDIT LOGGED | auditId={} | tool={} | actor={} | correlationId={} | status={} | humanConfirmed={}",
                logEntry.getAuditId(), tool, actor, correlationId, status, humanConfirmed);

        return logEntry;
    }

    @SuppressWarnings("unchecked")
    private String sanitizeAndSerialize(Object obj) {
        if (obj == null) return "{}";
        try {
            if (obj instanceof Map<?, ?> mapObj) {
                Map<String, Object> cleanMap = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : mapObj.entrySet()) {
                    String key = String.valueOf(entry.getKey());
                    if (isSecretKey(key)) {
                        cleanMap.put(key, "[REDACTED_SECRET]");
                    } else {
                        cleanMap.put(key, entry.getValue());
                    }
                }
                return objectMapper.writeValueAsString(cleanMap);
            }
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"error\":\"serialization_failed\"}";
        }
    }

    private boolean isSecretKey(String key) {
        if (key == null) return false;
        String lower = key.toLowerCase();
        return SECRET_FIELDS.stream().anyMatch(lower::contains);
    }
}
