package com.upimesh.bankgateway.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upimesh.bankgateway.exception.BankVerificationException;
import com.upimesh.bankgateway.model.enums.BankCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Getter
public class KotakBankClient extends AbstractBankClient {

    @Value("${banks.kotak.base-url}")
    private String baseUrl;

    @Value("${banks.kotak.mock-url}")
    private String mockUrl;

    @Value("${banks.kotak.api-key}")
    private String apiKey;

    @Value("${banks.kotak.client-id}")
    private String clientId;

    @Value("${banks.kotak.use-mock}")
    private boolean useMock;

    @Value("${banks.kotak.timeout-ms}")
    private int timeoutMs;

    public KotakBankClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    @Override
    public BankCode getBankCode() {
        return BankCode.KOTAK;
    }

    @Override
    @CircuitBreaker(name = "kotakBank", fallbackMethod = "verifyAccountFallback")
    @Retry(name = "bankDefault")
    public Map<String, String> verifyAccount(String accountNumber, String ifscCode) {
        if (isUseMock()) {
            return mockVerifyAccount(accountNumber, ifscCode);
        }

        Map<String, String> request = Map.of("accountNumber", accountNumber, "ifscCode", ifscCode);
        JsonNode response = doPost("/api/accounts/verify", request);

        Map<String, String> result = new HashMap<>();
        int code = response.path("code").asInt(0);

        if (code == 200) {
            JsonNode dataNode = response.path("data");
            result.put("holderName", dataNode.path("holderName").asText("Kotak Customer"));
            result.put("active", "true");
            result.put("accountType", dataNode.path("accountType").asText("SAVINGS"));
            result.put("referenceToken", dataNode.path("sessionId").asText());
            result.put("maskedAccount", maskAccount(accountNumber));
        } else {
            result.put("error", "Verification failed at Kotak. Code: " + code);
            result.put("message", response.path("message").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "kotakBank", fallbackMethod = "fetchBalanceFallback")
    @Retry(name = "bankDefault")
    public Map<String, Object> fetchBalance(String accountNumber, String ifscCode, String bankReferenceToken) {
        if (isUseMock()) {
            return mockFetchBalance(accountNumber);
        }

        String path = "/api/accounts/balance?token=" + bankReferenceToken;
        JsonNode response = doGet(path);

        Map<String, Object> result = new HashMap<>();
        int code = response.path("code").asInt(0);

        if (code == 200) {
            JsonNode dataNode = response.path("data");
            double avlBal = dataNode.path("availableBalance").asDouble(0.0);
            double ledBal = dataNode.path("currentBalance").asDouble(0.0); // Kotak uses currentBalance for ledger

            result.put("available", BigDecimal.valueOf(avlBal));
            result.put("ledger", BigDecimal.valueOf(ledBal));
            result.put("currency", dataNode.path("currency").asText("INR"));
        } else {
            result.put("error", "Fetch balance failed at Kotak. Code: " + code);
            result.put("message", response.path("message").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "kotakBank", fallbackMethod = "resolveUpiHandleFallback")
    @Retry(name = "bankDefault")
    public Map<String, String> resolveUpiHandle(String upiHandle) {
        if (isUseMock()) {
            return mockResolveUpiHandle(upiHandle);
        }

        Map<String, String> request = Map.of("upiAddress", upiHandle);
        JsonNode response = doPost("/api/upi/resolve", request);

        Map<String, String> result = new HashMap<>();
        int code = response.path("code").asInt(0);

        if (code == 200) {
            JsonNode dataNode = response.path("data");
            result.put("holderName", dataNode.path("holderName").asText("Kotak Customer"));
            result.put("maskedAccount", dataNode.path("maskedAccountNumber").asText("XXXX XXXX 1234"));
            result.put("ifscCode", dataNode.path("ifscCode").asText(BankCode.KOTAK.getIfscPrefix() + "0000123"));
            result.put("active", "true");
        } else {
            result.put("error", "UPI VPA resolution failed at Kotak. Code: " + code);
            result.put("message", response.path("message").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "kotakBank", fallbackMethod = "linkAccountFallback")
    @Retry(name = "bankDefault")
    public String linkAccount(String accountNumber, String ifscCode, String userUpiId) {
        if (isUseMock()) {
            return mockLinkAccount(accountNumber, userUpiId);
        }

        Map<String, String> request = Map.of(
                "accountNumber", accountNumber,
                "ifscCode", ifscCode,
                "upiAddress", userUpiId
        );
        JsonNode response = doPost("/api/upi/link", request);
        int code = response.path("code").asInt(0);

        if (code == 200) {
            return response.path("data").path("sessionId").asText();
        } else {
            throw new BankVerificationException("Failed to link account with Kotak. Code: " + code + ", Message: " + response.path("message").asText());
        }
    }

    // ─── Resilience4j Fallback Methods ──────────────────────────────────────────

    public Map<String, String> verifyAccountFallback(String accountNumber, String ifscCode, Throwable t) {
        log.error("Kotak verifyAccount fallback active. Error: {}", t.getMessage());
        Map<String, String> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "Kotak Bank API currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public Map<String, Object> fetchBalanceFallback(String accountNumber, String ifscCode, String bankReferenceToken, Throwable t) {
        log.error("Kotak fetchBalance fallback active. Error: {}", t.getMessage());
        Map<String, Object> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "Kotak Bank Balance API currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public Map<String, String> resolveUpiHandleFallback(String upiHandle, Throwable t) {
        log.error("Kotak resolveUpiHandle fallback active. Error: {}", t.getMessage());
        Map<String, String> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "Kotak Bank UPI resolution currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public String linkAccountFallback(String accountNumber, String ifscCode, String userUpiId, Throwable t) {
        log.error("Kotak linkAccount fallback active. Error: {}", t.getMessage());
        throw new BankVerificationException("Kotak Bank link account currently unavailable. Fallback error: " + t.getMessage(), t);
    }
}
