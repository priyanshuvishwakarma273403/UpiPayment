package com.fraudService.mcp.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Sensitive Action Tools requiring explicit Human-in-the-Loop Analyst confirmation.
 * AI cannot silently perform financial or account restriction actions.
 */
@Component
@Slf4j
public class McpSensitiveTools {

    public Map<String, Object> freezeAccount(Long customerId, String reason, String humanAnalystId) {
        log.info("MCP Sensitive Tool Executed: freeze_account | customerId={} | confirmedByHuman={}", customerId, humanAnalystId);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("action", "FREEZE_ACCOUNT");
        res.put("customerId", customerId);
        res.put("reason", reason);
        res.put("status", "ACCOUNT_FROZEN");
        res.put("humanAnalystId", humanAnalystId);
        res.put("executedAt", LocalDateTime.now().toString());
        return res;
    }

    public Map<String, Object> blockBeneficiary(String upiId, String reason, String humanAnalystId) {
        log.info("MCP Sensitive Tool Executed: block_beneficiary | upiId={} | confirmedByHuman={}", upiId, humanAnalystId);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("action", "BLOCK_BENEFICIARY");
        res.put("upiId", upiId);
        res.put("reason", reason);
        res.put("status", "BENEFICIARY_BLOCKED");
        res.put("humanAnalystId", humanAnalystId);
        res.put("executedAt", LocalDateTime.now().toString());
        return res;
    }

    public Map<String, Object> addWatchlist(String entityId, String entityType, String reason, String humanAnalystId) {
        log.info("MCP Sensitive Tool Executed: add_watchlist | entityId={} | type={} | confirmedByHuman={}", entityId, entityType, humanAnalystId);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("action", "ADD_WATCHLIST");
        res.put("entityId", entityId);
        res.put("entityType", entityType);
        res.put("reason", reason);
        res.put("status", "ADDED_TO_WATCHLIST");
        res.put("humanAnalystId", humanAnalystId);
        res.put("executedAt", LocalDateTime.now().toString());
        return res;
    }
}
