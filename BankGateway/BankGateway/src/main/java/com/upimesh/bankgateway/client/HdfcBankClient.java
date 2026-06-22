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
public class HdfcBankClient extends AbstractBankClient {

    @Value("${banks.hdfc.base-url}")
    private String baseUrl;

    @Value("${banks.hdfc.mock-url}")
    private String mockUrl;

    @Value("${banks.hdfc.api-key}")
    private String apiKey;

    @Value("${banks.hdfc.client-id}")
    private String clientId;

    @Value("${banks.hdfc.use-mock}")
    private boolean useMock;

    @Value("${banks.hdfc.timeout-ms}")
    private int timeoutMs;

    public HdfcBankClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    @Override
    public BankCode getBankCode() {
        return BankCode.HDFC;
    }

    @Override
    @CircuitBreaker(name = "hdfcBank", fallbackMethod = "verifyAccountFallback")
    @Retry(name = "bankDefault")
    public Map<String, String> verifyAccount(String accountNumber, String ifscCode) {
        if (isUseMock()) {
            return mockVerifyAccount(accountNumber, ifscCode);
        }

        Map<String, String> request = Map.of("accountNumber", accountNumber, "ifscCode", ifscCode);
        JsonNode response = doPost("/accounts/verify", request);

        Map<String, String> result = new HashMap<>();
        String statusCode = response.path("statusCode").asText();

        if ("00".equals(statusCode)) {
            result.put("holderName", response.path("accountName").isMissingNode() ? 
                    response.path("payeeName").asText("HDFC Customer") : response.path("accountName").asText());
            result.put("active", String.valueOf(response.path("isActive").asBoolean(true)));
            result.put("accountType", response.path("accountType").asText("SAVINGS"));
            result.put("referenceToken", response.path("referenceToken").asText());
            result.put("maskedAccount", maskAccount(accountNumber));
        } else {
            result.put("error", "Verification failed at HDFC Bank. Code: " + statusCode);
            result.put("message", response.path("message").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "hdfcBank", fallbackMethod = "fetchBalanceFallback")
    @Retry(name = "bankDefault")
    public Map<String, Object> fetchBalance(String accountNumber, String ifscCode, String bankReferenceToken) {
        if (isUseMock()) {
            return mockFetchBalance(accountNumber);
        }

        String path = "/accounts/balance?token=" + bankReferenceToken;
        JsonNode response = doGet(path);

        Map<String, Object> result = new HashMap<>();
        String statusCode = response.path("statusCode").asText();

        if ("00".equals(statusCode)) {
            // HDFC returns balance in PAISE (divide by 100)
            long avlPaise = response.path("availableBalance").asLong(0);
            long ledPaise = response.path("ledgerBalance").asLong(0);

            result.put("available", BigDecimal.valueOf(avlPaise).divide(BigDecimal.valueOf(100)));
            result.put("ledger", BigDecimal.valueOf(ledPaise).divide(BigDecimal.valueOf(100)));
            result.put("currency", response.path("currency").asText("INR"));
        } else {
            result.put("error", "Fetch balance failed at HDFC Bank. Code: " + statusCode);
            result.put("message", response.path("message").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "hdfcBank", fallbackMethod = "resolveUpiHandleFallback")
    @Retry(name = "bankDefault")
    public Map<String, String> resolveUpiHandle(String upiHandle) {
        if (isUseMock()) {
            return mockResolveUpiHandle(upiHandle);
        }

        Map<String, String> request = Map.of("vpa", upiHandle);
        JsonNode response = doPost("/upi/resolve-vpa", request);

        Map<String, String> result = new HashMap<>();
        String statusCode = response.path("statusCode").asText();

        if ("00".equals(statusCode)) {
            result.put("holderName", response.path("payeeName").asText("HDFC Customer"));
            result.put("maskedAccount", response.path("maskedAccountNumber").asText("XXXX XXXX 1234"));
            result.put("ifscCode", response.path("ifscCode").asText(BankCode.HDFC.getIfscPrefix() + "0000123"));
            result.put("active", String.valueOf(response.path("isActive").asBoolean(true)));
        } else {
            result.put("error", "UPI VPA resolution failed at HDFC Bank. Code: " + statusCode);
            result.put("message", response.path("message").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "hdfcBank", fallbackMethod = "linkAccountFallback")
    @Retry(name = "bankDefault")
    public String linkAccount(String accountNumber, String ifscCode, String userUpiId) {
        if (isUseMock()) {
            return mockLinkAccount(accountNumber, userUpiId);
        }

        Map<String, String> request = Map.of(
                "accountNumber", accountNumber,
                "ifscCode", ifscCode,
                "vpa", userUpiId
        );
        JsonNode response = doPost("/upi/link-account", request);
        String statusCode = response.path("statusCode").asText();

        if ("00".equals(statusCode)) {
            return response.path("referenceToken").asText();
        } else {
            throw new BankVerificationException("Failed to link account with HDFC Bank. Code: " + statusCode + ", Message: " + response.path("message").asText());
        }
    }

    // ─── Resilience4j Fallback Methods ──────────────────────────────────────────

    public Map<String, String> verifyAccountFallback(String accountNumber, String ifscCode, Throwable t) {
        log.error("HDFC verifyAccount fallback active. Error: {}", t.getMessage());
        Map<String, String> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "HDFC Bank API currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public Map<String, Object> fetchBalanceFallback(String accountNumber, String ifscCode, String bankReferenceToken, Throwable t) {
        log.error("HDFC fetchBalance fallback active. Error: {}", t.getMessage());
        Map<String, Object> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "HDFC Bank Balance API currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public Map<String, String> resolveUpiHandleFallback(String upiHandle, Throwable t) {
        log.error("HDFC resolveUpiHandle fallback active. Error: {}", t.getMessage());
        Map<String, String> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "HDFC Bank UPI resolution currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public String linkAccountFallback(String accountNumber, String ifscCode, String userUpiId, Throwable t) {
        log.error("HDFC linkAccount fallback active. Error: {}", t.getMessage());
        throw new BankVerificationException("HDFC Bank link account currently unavailable. Fallback error: " + t.getMessage(), t);
    }
}
