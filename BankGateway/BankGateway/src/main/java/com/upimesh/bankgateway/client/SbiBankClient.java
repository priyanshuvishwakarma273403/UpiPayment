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
public class SbiBankClient extends AbstractBankClient {

    @Value("${banks.sbi.base-url}")
    private String baseUrl;

    @Value("${banks.sbi.mock-url}")
    private String mockUrl;

    @Value("${banks.sbi.api-key}")
    private String apiKey;

    @Value("${banks.sbi.client-id}")
    private String clientId;

    @Value("${banks.sbi.use-mock}")
    private boolean useMock;

    @Value("${banks.sbi.timeout-ms}")
    private int timeoutMs;

    public SbiBankClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    @Override
    public BankCode getBankCode() {
        return BankCode.SBI;
    }

    @Override
    @CircuitBreaker(name = "sbiBank", fallbackMethod = "verifyAccountFallback")
    @Retry(name = "bankDefault")
    public Map<String, String> verifyAccount(String accountNumber, String ifscCode) {
        if (isUseMock()) {
            return mockVerifyAccount(accountNumber, ifscCode);
        }

        Map<String, String> request = Map.of("accountNumber", accountNumber, "ifscCode", ifscCode);
        JsonNode response = doPost("/retail/v2/account/verify", request);

        Map<String, String> result = new HashMap<>();
        String respCode = response.path("respCode").asText();

        if ("000".equals(respCode)) {
            result.put("holderName", response.path("acctName").asText("SBI Customer"));
            String status = response.path("acctStatus").asText("INACTIVE");
            result.put("active", String.valueOf("ACTIVE".equalsIgnoreCase(status)));
            result.put("accountType", response.path("acctType").asText("SAVINGS"));
            result.put("referenceToken", response.path("sessionToken").asText());
            result.put("maskedAccount", maskAccount(accountNumber));
        } else {
            result.put("error", "Verification failed at SBI. Code: " + respCode);
            result.put("message", response.path("respMsg").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "sbiBank", fallbackMethod = "fetchBalanceFallback")
    @Retry(name = "bankDefault")
    public Map<String, Object> fetchBalance(String accountNumber, String ifscCode, String bankReferenceToken) {
        if (isUseMock()) {
            return mockFetchBalance(accountNumber);
        }

        Map<String, String> request = Map.of(
                "accountNumber", accountNumber,
                "ifscCode", ifscCode,
                "sessionToken", bankReferenceToken
        );
        JsonNode response = doPost("/retail/v2/account/balance", request);

        Map<String, Object> result = new HashMap<>();
        String respCode = response.path("respCode").asText();

        if ("000".equals(respCode)) {
            // Balance in RUPEES directly
            double avlBal = response.path("avlBal").asDouble(0.0);
            double ledBal = response.path("ledBal").asDouble(0.0);

            result.put("available", BigDecimal.valueOf(avlBal));
            result.put("ledger", BigDecimal.valueOf(ledBal));
            result.put("currency", response.path("currency").asText("INR"));
        } else {
            result.put("error", "Fetch balance failed at SBI. Code: " + respCode);
            result.put("message", response.path("respMsg").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "sbiBank", fallbackMethod = "resolveUpiHandleFallback")
    @Retry(name = "bankDefault")
    public Map<String, String> resolveUpiHandle(String upiHandle) {
        if (isUseMock()) {
            return mockResolveUpiHandle(upiHandle);
        }

        Map<String, String> request = Map.of("upiId", upiHandle);
        JsonNode response = doPost("/upi/v1/resolve", request);

        Map<String, String> result = new HashMap<>();
        String respCode = response.path("respCode").asText();

        if ("000".equals(respCode)) {
            result.put("holderName", response.path("acctName").asText("SBI Customer"));
            result.put("maskedAccount", response.path("maskedAccountNumber").asText("XXXX XXXX 1234"));
            result.put("ifscCode", response.path("ifscCode").asText(BankCode.SBI.getIfscPrefix() + "0000123"));
            String status = response.path("acctStatus").asText("INACTIVE");
            result.put("active", String.valueOf("ACTIVE".equalsIgnoreCase(status)));
        } else {
            result.put("error", "UPI VPA resolution failed at SBI. Code: " + respCode);
            result.put("message", response.path("respMsg").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "sbiBank", fallbackMethod = "linkAccountFallback")
    @Retry(name = "bankDefault")
    public String linkAccount(String accountNumber, String ifscCode, String userUpiId) {
        if (isUseMock()) {
            return mockLinkAccount(accountNumber, userUpiId);
        }

        Map<String, String> request = Map.of(
                "accountNumber", accountNumber,
                "ifscCode", ifscCode,
                "upiId", userUpiId
        );
        JsonNode response = doPost("/upi/v1/register", request);
        String respCode = response.path("respCode").asText();

        if ("000".equals(respCode)) {
            return response.path("sessionToken").asText();
        } else {
            throw new BankVerificationException("Failed to link account with SBI. Code: " + respCode + ", Message: " + response.path("respMsg").asText());
        }
    }

    // ─── Resilience4j Fallback Methods ──────────────────────────────────────────

    public Map<String, String> verifyAccountFallback(String accountNumber, String ifscCode, Throwable t) {
        log.error("SBI verifyAccount fallback active. Error: {}", t.getMessage());
        Map<String, String> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "SBI Bank API currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public Map<String, Object> fetchBalanceFallback(String accountNumber, String ifscCode, String bankReferenceToken, Throwable t) {
        log.error("SBI fetchBalance fallback active. Error: {}", t.getMessage());
        Map<String, Object> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "SBI Bank Balance API currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public Map<String, String> resolveUpiHandleFallback(String upiHandle, Throwable t) {
        log.error("SBI resolveUpiHandle fallback active. Error: {}", t.getMessage());
        Map<String, String> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "SBI Bank UPI resolution currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public String linkAccountFallback(String accountNumber, String ifscCode, String userUpiId, Throwable t) {
        log.error("SBI linkAccount fallback active. Error: {}", t.getMessage());
        throw new BankVerificationException("SBI Bank link account currently unavailable. Fallback error: " + t.getMessage(), t);
    }
}
