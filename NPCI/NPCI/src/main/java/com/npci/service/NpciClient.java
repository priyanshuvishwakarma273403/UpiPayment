package com.npci.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * NpciClient — Handles all HTTP communication with NPCI.
 *
 * In production: uses real NPCI APIs with mutual TLS
 * In dev/test:   uses mock server (use-mock: true in config)
 *
 * Circuit breaker wraps all calls — if NPCI is down,
 * we fail fast instead of hanging the user's request.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class NpciClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PayloadSigningService signingService;

    @Value("${npci.base-url}")
    private String npciBaseUrl;

    @Value("${npci.mock-url}")
    private String mockUrl;

    @Value("${npci.use-mock}")
    private boolean useMock;

    @Value("${npci.api-key}")
    private String apiKey;

    @Value("${npci.merchant-id}")
    private String merchantId;

    // ─── Transaction APIs ─────────────────────────────────────────────────────

    /**
     * Send payment request to NPCI.
     * Returns NPCI's raw JSON response node.
     */
    @CircuitBreaker(name = "npciService", fallbackMethod = "initiateTransactionFallback")
    @Retry(name = "npciService")
    public JsonNode initiateTransaction(String senderUpiId, String receiverUpiId,
                                        BigDecimal amount, String txnId,
                                        String remarks, String mpinHash) {
        String url = baseUrl() + "/transaction/pay";

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("merchantId", merchantId);
        payload.put("txnId", txnId);
        payload.put("payerAddr", senderUpiId);         // NPCI term for sender UPI
        payload.put("payeeAddr", receiverUpiId);        // NPCI term for receiver UPI
        payload.put("amount", amount.toPlainString());
        payload.put("currency", "INR");
        payload.put("remarks", remarks != null ? remarks : "UPI Payment");
        payload.put("txnNote", remarks != null ? remarks : "UPI Payment");
        payload.put("refId", UUID.randomUUID().toString().replace("-", "").substring(0, 12));

        // Sign payload with our RSA private key (NPCI requirement)
        String signature = signingService.sign(payload.toString());
        payload.put("signature", signature);
        payload.put("mpinHash", mpinHash);

        log.info("Sending initiate-transaction to NPCI | txnId={} | amount={}", txnId, amount);
        return postToNpci(url, payload);
    }

    /**
     * Check status of a transaction by NPCI txn ID
     */
    @CircuitBreaker(name = "npciService", fallbackMethod = "checkStatusFallback")
    public JsonNode checkTransactionStatus(String npciTxnId) {
        String url = baseUrl() + "/transaction/status/" + npciTxnId;

        HttpHeaders headers = buildHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        log.info("Checking NPCI transaction status | npciTxnId={}", npciTxnId);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class);
        return response.getBody();
    }

    /**
     * Initiate refund for a completed transaction
     */
    @CircuitBreaker(name = "npciService", fallbackMethod = "refundFallback")
    @Retry(name = "npciService")
    public JsonNode initiateRefund(String originalNpciTxnId, String refundId,
                                   BigDecimal amount, String reason) {
        String url = baseUrl() + "/transaction/refund";

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("merchantId", merchantId);
        payload.put("orgTxnId", originalNpciTxnId);
        payload.put("refundTxnId", refundId);
        payload.put("refundAmount", amount.toPlainString());
        payload.put("remarks", reason);

        String signature = signingService.sign(payload.toString());
        payload.put("signature", signature);

        log.info("Initiating refund to NPCI | originalTxn={} | amount={}", originalNpciTxnId, amount);
        return postToNpci(url, payload);
    }

    /**
     * Create a recurring UPI AutoPay mandate
     */
    @CircuitBreaker(name = "npciService", fallbackMethod = "mandateFallback")
    public JsonNode createMandate(String userUpiId, String merchantUpiId,
                                  BigDecimal maxAmount, String frequency,
                                  LocalDate startDate, LocalDate endDate,
                                  String purpose, String mandateId) {
        String url = baseUrl() + "/mandate/create";

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("merchantId", merchantId);
        payload.put("mandateId", mandateId);
        payload.put("payerAddr", userUpiId);
        payload.put("payeeAddr", merchantUpiId);
        payload.put("maxAmount", maxAmount.toPlainString());
        payload.put("frequency", frequency);
        payload.put("startDate", startDate.toString());
        payload.put("endDate", endDate.toString());
        payload.put("purpose", purpose);
        payload.put("currency", "INR");

        String signature = signingService.sign(payload.toString());
        payload.put("signature", signature);

        log.info("Creating mandate on NPCI | mandateId={} | user={}", mandateId, userUpiId);
        return postToNpci(url, payload);
    }

    // ─── Fallback methods (when NPCI is down) ────────────────────────────────

    public JsonNode initiateTransactionFallback(String s, String r, BigDecimal a,
                                                String t, String rem, String m, Throwable ex) {
        log.error("NPCI circuit open — initiateTransaction fallback | error={}", ex.getMessage());
        return buildErrorResponse("NPCI_UNAVAILABLE", "Payment service temporarily unavailable. Please retry.");
    }

    public JsonNode checkStatusFallback(String npciTxnId, Throwable ex) {
        log.error("NPCI circuit open — checkStatus fallback | txnId={}", npciTxnId);
        return buildErrorResponse("NPCI_UNAVAILABLE", "Status check failed. Transaction state unknown.");
    }

    public JsonNode refundFallback(String o, String r, BigDecimal a, String reason, Throwable ex) {
        log.error("NPCI circuit open — refund fallback | error={}", ex.getMessage());
        return buildErrorResponse("NPCI_UNAVAILABLE", "Refund queued. Will process when NPCI is available.");
    }

    public JsonNode mandateFallback(String u, String m, BigDecimal a, String f,
                                    LocalDate s, LocalDate e, String p, String id, Throwable ex) {
        log.error("NPCI circuit open — mandate fallback | error={}", ex.getMessage());
        return buildErrorResponse("NPCI_UNAVAILABLE", "Mandate creation failed. Please retry later.");
    }

    // ─── Internal helpers ─────────────────────────────────────────────────────

    private JsonNode postToNpci(String url, ObjectNode payload) {
        HttpHeaders headers = buildHeaders();
        HttpEntity<ObjectNode> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);
        return response.getBody();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-KEY", apiKey);
        headers.set("X-Merchant-ID", merchantId);
        headers.set("X-Request-ID", UUID.randomUUID().toString());
        return headers;
    }

    private String baseUrl() {
        return useMock ? mockUrl : npciBaseUrl;
    }

    private JsonNode buildErrorResponse(String code, String message) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("status", "FAILURE");
        node.put("responseCode", code);
        node.put("message", message);
        return node;
    }
}
