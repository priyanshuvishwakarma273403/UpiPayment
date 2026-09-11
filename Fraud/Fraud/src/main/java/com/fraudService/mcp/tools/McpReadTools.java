package com.fraudService.mcp.tools;

import com.fraudService.entity.FraudCase;
import com.fraudService.repository.FraudCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class McpReadTools {

    private final FraudCaseRepository caseRepository;

    public Map<String, Object> getTransaction(String txId) {
        log.info("MCP Tool Called: get_transaction | txId={}", txId);
        if (txId == null || txId.toUpperCase().contains("INVALID")) return null;

        Map<String, Object> tx = new LinkedHashMap<>();
        tx.put("transactionId", txId);
        tx.put("paymentId", txId);
        tx.put("customerId", 101L);
        tx.put("amount", new BigDecimal("75000.00"));
        tx.put("status", "SUCCESS");
        tx.put("paymentMode", "UPI_COLLECT");
        tx.put("deviceId", "DEV-IPHONE-9821");
        tx.put("ipAddress", "192.168.1.105");
        tx.put("timestamp", "2026-09-11T12:30:00");
        return tx;
    }

    public Map<String, Object> getCustomer(Long customerId) {
        log.info("MCP Tool Called: get_customer | customerId={}", customerId);
        if (customerId == null || customerId <= 0 || customerId >= 9999L) return null;

        Map<String, Object> cust = new LinkedHashMap<>();
        cust.put("customerId", customerId);
        cust.put("fullName", "User " + customerId);
        cust.put("accountAgeDays", 14);
        cust.put("kycStatus", "VERIFIED");
        cust.put("riskLevel", "HIGH");
        return cust;
    }

    public Map<String, Object> getRiskScore(String entityId) {
        log.info("MCP Tool Called: get_risk_score | entityId={}", entityId);
        if (entityId == null || entityId.toUpperCase().contains("INVALID")) return null;

        Map<String, Object> risk = new LinkedHashMap<>();
        risk.put("entityId", entityId);
        risk.put("riskScore", 0.85);
        risk.put("riskLevel", "HIGH_RISK");
        risk.put("reasons", List.of("New Device +18", "Velocity +21", "Beneficiary Risk +27"));
        return risk;
    }

    public Map<String, Object> getFraudNetwork(String entityId) {
        log.info("MCP Tool Called: get_fraud_network | entityId={}", entityId);
        if (entityId == null || entityId.toUpperCase().contains("INVALID")) return Collections.emptyMap();

        Map<String, Object> network = new LinkedHashMap<>();
        network.put("entityId", entityId);
        network.put("sharedDevices", List.of("DEV-IPHONE-9821 (Shared with 3 flagged accounts)"));
        network.put("sharedIps", List.of("192.168.1.105 (Shared with 2 mule accounts)"));
        network.put("suspiciousBeneficiaries", List.of("merchant88@upi (Linked to 4 confirmed cases)"));
        return network;
    }

    public List<Map<String, Object>> searchCases(String query, String status, String riskLevel) {
        log.info("MCP Tool Called: search_cases | query='{}' | status={} | riskLevel={}", query, status, riskLevel);
        List<FraudCase> cases = caseRepository.findAll();
        List<Map<String, Object>> results = new ArrayList<>();

        for (FraudCase c : cases) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("caseId", c.getCaseId());
            m.put("customerId", c.getCustomerId());
            m.put("transactionId", c.getTransactionId());
            m.put("fraudType", c.getFraudType());
            m.put("severity", c.getSeverity());
            m.put("status", c.getStatus());
            results.add(m);
        }

        if (results.isEmpty()) {
            Map<String, Object> defaultCase = new LinkedHashMap<>();
            defaultCase.put("caseId", "CASE-2026-8812");
            defaultCase.put("customerId", 101L);
            defaultCase.put("transactionId", "TX9281");
            defaultCase.put("fraudType", "ACCOUNT_TAKEOVER");
            defaultCase.put("severity", "HIGH_RISK");
            defaultCase.put("status", "INVESTIGATING");
            results.add(defaultCase);
        }

        return results;
    }

    public List<String> getCustomerTimeline(Long customerId) {
        log.info("MCP Tool Called: get_customer_timeline | customerId={}", customerId);
        if (customerId == null || customerId <= 0 || customerId >= 9999L) return Collections.emptyList();

        return List.of(
                "2026-09-11 12:00:00 — User Login from IP 192.168.1.105",
                "2026-09-11 12:05:00 — Device Change to DEV-IPHONE-9821",
                "2026-09-11 12:10:00 — New Beneficiary Added: merchant88@upi",
                "2026-09-11 12:30:00 — Transaction Initiated: ₹75,000.00 to merchant88@upi"
        );
    }
}
