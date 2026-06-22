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
public class AxisBankClient extends AbstractBankClient {

    @Value("${banks.axis.base-url}")
    private String baseUrl;

    @Value("${banks.axis.mock-url}")
    private String mockUrl;

    @Value("${banks.axis.api-key}")
    private String apiKey;

    @Value("${banks.axis.client-id}")
    private String clientId;

    @Value("${banks.axis.use-mock}")
    private boolean useMock;

    @Value("${banks.axis.timeout-ms}")
    private int timeoutMs;

    public AxisBankClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    @Override
    public BankCode getBankCode() {
        return BankCode.AXIS;
    }

    @Override
    @CircuitBreaker(name = "axisBank", fallbackMethod = "verifyAccountFallback")
    @Retry(name = "bankDefault")
    public Map<String, String> verifyAccount(String accountNumber, String ifscCode) {
        if (isUseMock()) {
            return mockVerifyAccount(accountNumber, ifscCode);
        }

        Map<String, String> request = Map.of("accountNumber", accountNumber, "ifscCode", ifscCode);
        JsonNode response = doPost("/openbanking/v2/accounts/verify", request);

        Map<String, String> result = new HashMap<>();
        String responseCode = response.path("responseCode").asText();

        if ("0".equals(responseCode)) {
            result.put("holderName", response.path("accountHolderName").isMissingNode() ? 
                    response.path("registeredName").asText("Axis Customer") : response.path("accountHolderName").asText());
            result.put("active", "true");
            result.put("accountType", response.path("accountType").asText("SAVINGS"));
            result.put("referenceToken", response.path("authToken").asText());
            result.put("maskedAccount", response.path("maskedAcctNum").isMissingNode() ? 
                    maskAccount(accountNumber) : response.path("maskedAcctNum").asText());
        } else {
            result.put("error", "Verification failed at Axis. Code: " + responseCode);
            result.put("message", response.path("message").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "axisBank", fallbackMethod = "fetchBalanceFallback")
    @Retry(name = "bankDefault")
    public Map<String, Object> fetchBalance(String accountNumber, String ifscCode, String bankReferenceToken) {
        if (isUseMock()) {
            return mockFetchBalance(accountNumber);
        }

        String path = "/openbanking/v2/accounts/balance?token=" + bankReferenceToken;
        JsonNode response = doGet(path);

        Map<String, Object> result = new HashMap<>();
        String responseCode = response.path("responseCode").asText();

        if ("0".equals(responseCode)) {
            // Balance in PAISE (divide by 100)
            long avlPaise = response.path("availableBalance").asLong(0);
            long ledPaise = response.path("ledgerBalance").asLong(0);

            result.put("available", BigDecimal.valueOf(avlPaise).divide(BigDecimal.valueOf(100)));
            result.put("ledger", BigDecimal.valueOf(ledPaise).divide(BigDecimal.valueOf(100)));
            result.put("currency", response.path("currency").asText("INR"));
        } else {
            result.put("error", "Fetch balance failed at Axis. Code: " + responseCode);
            result.put("message", response.path("message").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "axisBank", fallbackMethod = "resolveUpiHandleFallback")
    @Retry(name = "bankDefault")
    public Map<String, String> resolveUpiHandle(String upiHandle) {
        if (isUseMock()) {
            return mockResolveUpiHandle(upiHandle);
        }

        Map<String, String> request = Map.of("upiVirtualAddress", upiHandle);
        JsonNode response = doPost("/openbanking/v2/upi/resolve", request);

        Map<String, String> result = new HashMap<>();
        String responseCode = response.path("responseCode").asText();

        if ("0".equals(responseCode)) {
            result.put("holderName", response.path("registeredName").isMissingNode() ? 
                    response.path("accountHolderName").asText("Axis Customer") : response.path("registeredName").asText());
            result.put("maskedAccount", response.path("maskedAcctNum").asText("XXXX XXXX 1234"));
            result.put("ifscCode", response.path("ifscCode").asText(BankCode.AXIS.getIfscPrefix() + "0000123"));
            result.put("active", "true");
        } else {
            result.put("error", "UPI VPA resolution failed at Axis. Code: " + responseCode);
            result.put("message", response.path("message").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "axisBank", fallbackMethod = "linkAccountFallback")
    @Retry(name = "bankDefault")
    public String linkAccount(String accountNumber, String ifscCode, String userUpiId) {
        if (isUseMock()) {
            return mockLinkAccount(accountNumber, userUpiId);
        }

        Map<String, String> request = Map.of(
                "accountNumber", accountNumber,
                "ifscCode", ifscCode,
                "upiVirtualAddress", userUpiId
        );
        JsonNode response = doPost("/openbanking/v2/upi/link", request);
        String responseCode = response.path("responseCode").asText();

        if ("0".equals(responseCode)) {
            return response.path("authToken").asText();
        } else {
            throw new BankVerificationException("Failed to link account with Axis. Code: " + responseCode + ", Message: " + response.path("message").asText());
        }
    }

    // ─── Resilience4j Fallback Methods ──────────────────────────────────────────

    public Map<String, String> verifyAccountFallback(String accountNumber, String ifscCode, Throwable t) {
        log.error("Axis verifyAccount fallback active. Error: {}", t.getMessage());
        Map<String, String> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "Axis Bank API currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public Map<String, Object> fetchBalanceFallback(String accountNumber, String ifscCode, String bankReferenceToken, Throwable t) {
        log.error("Axis fetchBalance fallback active. Error: {}", t.getMessage());
        Map<String, Object> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "Axis Bank Balance API currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public Map<String, String> resolveUpiHandleFallback(String upiHandle, Throwable t) {
        log.error("Axis resolveUpiHandle fallback active. Error: {}", t.getMessage());
        Map<String, String> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "Axis Bank UPI resolution currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public String linkAccountFallback(String accountNumber, String ifscCode, String userUpiId, Throwable t) {
        log.error("Axis linkAccount fallback active. Error: {}", t.getMessage());
        throw new BankVerificationException("Axis Bank link account currently unavailable. Fallback error: " + t.getMessage(), t);
    }
}
