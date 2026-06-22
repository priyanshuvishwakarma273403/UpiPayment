package com.upimesh.bankgateway.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.upimesh.bankgateway.model.enums.BankCode;
import com.upimesh.bankgateway.util.AccountEncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
public abstract class AbstractBankClient implements BankClient {

    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper;
    protected final Random random = new Random();

    protected AbstractBankClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    protected abstract String getBaseUrl();
    protected abstract String getMockUrl();
    protected abstract boolean isUseMock();
    protected abstract String getApiKey();
    protected abstract String getClientId();

    protected String resolvedBaseUrl() {
        return isUseMock() ? getMockUrl() : getBaseUrl();
    }

    protected HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", getApiKey());
        headers.set("X-Client-ID", getClientId());
        headers.set("X-Request-ID", UUID.randomUUID().toString());
        headers.set("X-Timestamp", String.valueOf(System.currentTimeMillis()));
        return headers;
    }

    protected JsonNode doGet(String path) {
        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String url = resolvedBaseUrl() + path;
            log.info("Sending GET request to bank API | url={}", url);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("HTTP error during GET | path={} | status={} | response={}", path, e.getStatusCode(), e.getResponseBodyAsString());
            return buildErrorNode(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Connection error during GET | path={} | error={}", path, e.getMessage());
            return buildErrorNode(503, "Bank API unreachable: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during GET | path={} | error={}", path, e.getMessage());
            return buildErrorNode(500, "Internal error: " + e.getMessage());
        }
    }

    protected JsonNode doPost(String path, Object requestBody) {
        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
            String url = resolvedBaseUrl() + path;
            log.info("Sending POST request to bank API | url={}", url);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("HTTP error during POST | path={} | status={} | response={}", path, e.getStatusCode(), e.getResponseBodyAsString());
            return buildErrorNode(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Connection error during POST | path={} | error={}", path, e.getMessage());
            return buildErrorNode(503, "Bank API unreachable: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during POST | path={} | error={}", path, e.getMessage());
            return buildErrorNode(500, "Internal error: " + e.getMessage());
        }
    }

    protected JsonNode buildErrorNode(int status, String message) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("error", true);
        node.put("status", status);
        node.put("message", message);
        return node;
    }

    protected String maskAccount(String accountNumber) {
        return AccountEncryptionUtil.mask(accountNumber);
    }

    protected String buildMaskedAccount(String accountNumber) {
        return maskAccount(accountNumber);
    }

    // Dev Mode Mock Implementations
    protected Map<String, String> mockVerifyAccount(String accountNumber, String ifscCode) {
        log.info("Mocking verifyAccount for bank {} | account={}", getBankCode(), maskAccount(accountNumber));
        Map<String, String> result = new HashMap<>();
        result.put("holderName", "Test User (" + getBankCode().getDisplayName() + ")");
        result.put("active", "true");
        result.put("accountType", "SAVINGS");
        result.put("referenceToken", "REF-" + getBankCode().name() + "-" + (10000000 + random.nextInt(90000000)));
        result.put("maskedAccount", maskAccount(accountNumber));
        return result;
    }

    protected Map<String, Object> mockFetchBalance(String accountNumber) {
        log.info("Mocking fetchBalance for bank {} | account={}", getBankCode(), maskAccount(accountNumber));
        BigDecimal available = BigDecimal.valueOf(1000 + random.nextInt(99000));
        BigDecimal ledger = available.add(BigDecimal.valueOf(500));
        
        Map<String, Object> result = new HashMap<>();
        result.put("available", available);
        result.put("ledger", ledger);
        result.put("currency", "INR");
        return result;
    }

    protected Map<String, String> mockResolveUpiHandle(String upiHandle) {
        log.info("Mocking resolveUpiHandle for bank {} | handle={}", getBankCode(), upiHandle);
        String localPart = upiHandle.split("@")[0];
        Map<String, String> result = new HashMap<>();
        result.put("holderName", "Mock User - " + localPart);
        result.put("maskedAccount", "XXXX XXXX 1234");
        result.put("ifscCode", getBankCode().getIfscPrefix() + "0000123");
        result.put("active", "true");
        return result;
    }

    protected String mockLinkAccount(String accountNumber, String userUpiId) {
        log.info("Mocking linkAccount for bank {} | account={} | upi={}", getBankCode(), maskAccount(accountNumber), userUpiId);
        return "MOCK-TOKEN-" + getBankCode().name() + "-" + (100000000000L + (long)(random.nextDouble() * 900000000000L));
    }
}
