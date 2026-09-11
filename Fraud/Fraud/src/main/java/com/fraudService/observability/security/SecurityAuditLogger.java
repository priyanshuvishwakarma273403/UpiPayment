package com.fraudService.observability.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Security Audit Logger & Secret Redacting Engine for Phase 21.
 * Audits authentication, authorization, CORS, rate limiting, input validation, JWT handling, and service-to-service calls.
 * Automatically redacts API keys, passwords, JWT secrets, database credentials, and tokens.
 */
@Component
@Slf4j
public class SecurityAuditLogger {

    private static final List<String> SECRET_KEYWORDS = List.of(
            "password", "passwd", "secret", "token", "apikey", "api_key",
            "jwt", "private_key", "pin", "cvv", "bearer", "authorization"
    );

    private static final Pattern SECRET_KV_PATTERN = Pattern.compile(
            "(?i)(password|secret|api_?key|token|jwt|pin|cvv|authorization|bearer)\\s*[:=]\\s*['\"]?([^\\s'\",}&;]+)['\"]?"
    );

    /**
     * Redacts secret parameters from log strings and key-value maps.
     */
    public String sanitizeLog(String input) {
        if (input == null) return null;
        return SECRET_KV_PATTERN.matcher(input).replaceAll("$1=[REDACTED]");
    }

    public Map<String, Object> sanitizeMap(Map<String, Object> inputMap) {
        if (inputMap == null) return Map.of();
        Map<String, Object> sanitized = new HashMap<>();

        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            boolean isSecretKey = SECRET_KEYWORDS.stream()
                    .anyMatch(kw -> key.toLowerCase().contains(kw));

            if (isSecretKey) {
                sanitized.put(key, "[REDACTED]");
            } else if (value instanceof String strVal) {
                sanitized.put(key, sanitizeLog(strVal));
            } else {
                sanitized.put(key, value);
            }
        }
        return sanitized;
    }

    public Map<String, Object> logAuditEvent(String category, String action, String actor, boolean success, Map<String, Object> details) {
        Map<String, Object> cleanDetails = sanitizeMap(details);

        Map<String, Object> auditRecord = new LinkedHashMap<>();
        auditRecord.put("eventId", "AUDIT_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6));
        auditRecord.put("timestamp", LocalDateTime.now().toString());
        auditRecord.put("category", category);
        auditRecord.put("action", action);
        auditRecord.put("actor", actor != null ? actor : "SYSTEM");
        auditRecord.put("status", success ? "SUCCESS" : "DENIED");
        auditRecord.put("details", cleanDetails);

        if (success) {
            log.info("[SECURITY AUDIT] category={}, action={}, actor={}, status=SUCCESS, details={}",
                    category, action, actor, cleanDetails);
        } else {
            log.warn("[SECURITY AUDIT ALERT] category={}, action={}, actor={}, status=DENIED, details={}",
                    category, action, actor, cleanDetails);
        }

        return auditRecord;
    }
}
