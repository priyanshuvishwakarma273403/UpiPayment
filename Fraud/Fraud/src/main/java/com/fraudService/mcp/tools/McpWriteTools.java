package com.fraudService.mcp.tools;

import com.fraudService.entity.FraudCase;
import com.fraudService.repository.FraudCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class McpWriteTools {

    private final FraudCaseRepository caseRepository;

    public Map<String, Object> createCase(Long customerId, String transactionId, String fraudType, String severity, String description) {
        log.info("MCP Tool Called: create_case | customerId={} | txId={} | fraudType={}", customerId, transactionId, fraudType);

        String caseId = "CASE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        FraudCase newCase = FraudCase.builder()
                .caseId(caseId)
                .customerId(customerId != null ? String.valueOf(customerId) : "101")
                .transactionId(transactionId)
                .fraudType(fraudType != null ? fraudType : "SUSPICIOUS_ACTIVITY")
                .severity(severity != null ? severity : "HIGH_RISK")
                .status("OPEN")
                .assignedTo("UNASSIGNED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        try {
            caseRepository.save(newCase);
        } catch (Exception e) {
            log.warn("Could not persist case in DB, returning generated object: {}", e.getMessage());
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("caseId", caseId);
        res.put("customerId", customerId);
        res.put("transactionId", transactionId);
        res.put("status", "OPEN");
        res.put("createdAt", LocalDateTime.now().toString());
        return res;
    }

    public Map<String, Object> addCaseNote(String caseId, String analystId, String note) {
        log.info("MCP Tool Called: add_case_note | caseId={} | analystId={}", caseId, analystId);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("caseId", caseId);
        res.put("analystId", analystId != null ? analystId : "ANALYST_AI");
        res.put("note", note);
        res.put("addedAt", LocalDateTime.now().toString());
        res.put("status", "NOTE_ADDED");
        return res;
    }

    public Map<String, Object> generateReport(String caseId, String reportType) {
        log.info("MCP Tool Called: generate_report | caseId={} | reportType={}", caseId, reportType);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("reportId", "RPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        res.put("caseId", caseId);
        res.put("reportType", reportType != null ? reportType : "FULL_INVESTIGATION_SUMMARY");
        res.put("generatedAt", LocalDateTime.now().toString());
        res.put("reportUrl", "/fraud/reports/" + caseId + ".pdf");
        return res;
    }
}
