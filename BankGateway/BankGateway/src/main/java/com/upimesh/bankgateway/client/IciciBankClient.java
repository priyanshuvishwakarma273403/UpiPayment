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
public class IciciBankClient extends AbstractBankClient {

    @Value("${banks.icici.base-url}")
    private String baseUrl;

    @Value("${banks.icici.mock-url}")
    private String mockUrl;

    @Value("${banks.icici.api-key}")
    private String apiKey;

    @Value("${banks.icici.client-id}")
    private String clientId;

    @Value("${banks.icici.use-mock}")
    private boolean useMock;

    @Value("${banks.icici.timeout-ms}")
    private int timeoutMs;

    public IciciBankClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    @Override
    public BankCode getBankCode() {
        return BankCode.ICICI;
    }

    @Override
    @CircuitBreaker(name = "iciciBank", fallbackMethod = "verifyAccountFallback")
    @Retry(name = "bankDefault")
    public Map<String, String> verifyAccount(String accountNumber, String ifscCode) {
        if (isUseMock()) {
            return mockVerifyAccount(accountNumber, ifscCode);
        }

        Map<String, String> request = Map.of("accountNumber", accountNumber, "ifscCode", ifscCode);
        JsonNode response = doPost("/api/v1/accounts/validate", request);

        Map<String, String> result = new HashMap<>();
        String status = response.path("status").asText();

        if ("SUCCESS".equalsIgnoreCase(status)) {
            result.put("holderName", response.path("customerName").isMissingNode() ? 
                    response.path("beneficiaryName").asText("ICICI Customer") : response.path("customerName").asText());
            result.put("active", "true");
            result.put("accountType", response.path("accountType").asText("SAVINGS"));
            result.put("referenceToken", response.path("linkToken").asText());
            result.put("maskedAccount", response.path("maskedAccountNumber").isMissingNode() ? 
                    maskAccount(accountNumber) : response.path("maskedAccountNumber").asText());
        } else {
            result.put("error", "Verification failed at ICICI. Status: " + status);
            result.put("message", response.path("errorMessage").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "iciciBank", fallbackMethod = "fetchBalanceFallback")
    @Retry(name = "bankDefault")
    public Map<String, Object> fetchBalance(String accountNumber, String ifscCode, String bankReferenceToken) {
        if (isUseMock()) {
            return mockFetchBalance(accountNumber);
        }

        Map<String, String> request = Map.of(
                "accountNumber", accountNumber,
                "ifscCode", ifscCode,
                "linkToken", bankReferenceToken
        );
        JsonNode response = doPost("/api/v1/accounts/balance", request);

        Map<String, Object> result = new HashMap<>();
        String status = response.path("status").asText();

        if ("SUCCESS".equalsIgnoreCase(status)) {
            // Balance in RUPEES as decimal string
            String avlStr = response.path("availableBalance").asText("0.00");
            String ledStr = response.path("bookBalance").asText("0.00");

            result.put("available", new BigDecimal(avlStr));
            result.put("ledger", new BigDecimal(ledStr));
            result.put("currency", response.path("currency").asText("INR"));
        } else {
            result.put("error", "Fetch balance failed at ICICI. Status: " + status);
            result.put("message", response.path("errorMessage").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "iciciBank", fallbackMethod = "resolveUpiHandleFallback")
    @Retry(name = "bankDefault")
    public Map<String, String> resolveUpiHandle(String upiHandle) {
        if (isUseMock()) {
            return mockResolveUpiHandle(upiHandle);
        }

        Map<String, String> request = Map.of("virtualPaymentAddress", upiHandle);
        JsonNode response = doPost("/api/v1/upi/vpa/validate", request);

        Map<String, String> result = new HashMap<>();
        String status = response.path("status").asText();

        if ("SUCCESS".equalsIgnoreCase(status)) {
            String vpaStatus = response.path("vpaStatus").asText("ACTIVE");
            boolean isActive = !"INACTIVE".equalsIgnoreCase(vpaStatus);

            result.put("holderName", response.path("customerName").isMissingNode() ?
                    response.path("beneficiaryName").asText("ICICI Customer") : response.path("customerName").asText());
            result.put("maskedAccount", response.path("maskedAccountNumber").asText("XXXX XXXX 1234"));
            result.put("ifscCode", response.path("ifscCode").asText(BankCode.ICICI.getIfscPrefix() + "0000123"));
            result.put("active", String.valueOf(isActive));
        } else {
            result.put("error", "UPI VPA resolution failed at ICICI. Status: " + status);
            result.put("message", response.path("errorMessage").asText("Unknown error"));
        }
        return result;
    }

    @Override
    @CircuitBreaker(name = "iciciBank", fallbackMethod = "linkAccountFallback")
    @Retry(name = "bankDefault")
    public String linkAccount(String accountNumber, String ifscCode, String userUpiId) {
        if (isUseMock()) {
            return mockLinkAccount(accountNumber, userUpiId);
        }

        Map<String, String> request = Map.of(
                "accountNumber", accountNumber,
                "ifscCode", ifscCode,
                "virtualPaymentAddress", userUpiId
        );
        JsonNode response = doPost("/api/v1/upi/account/link", request);
        String status = response.path("status").asText();

        if ("SUCCESS".equalsIgnoreCase(status)) {
            return response.path("linkToken").asText();
        } else {
            throw new BankVerificationException("Failed to link account with ICICI. Status: " + status + ", Message: " + response.path("errorMessage").asText());
        }
    }

    // ─── Resilience4j Fallback Methods ──────────────────────────────────────────

    public Map<String, String> verifyAccountFallback(String accountNumber, String ifscCode, Throwable t) {
        log.error("ICICI verifyAccount fallback active. Error: {}", t.getMessage());
        Map<String, String> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "ICICI Bank API currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public Map<String, Object> fetchBalanceFallback(String accountNumber, String ifscCode, String bankReferenceToken, Throwable t) {
        log.error("ICICI fetchBalance fallback active. Error: {}", t.getMessage());
        Map<String, Object> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "ICICI Bank Balance API currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public Map<String, String> resolveUpiHandleFallback(String upiHandle, Throwable t) {
        log.error("ICICI resolveUpiHandle fallback active. Error: {}", t.getMessage());
        Map<String, String> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "ICICI Bank UPI resolution currently unavailable (Fallback)");
        fallbackResult.put("message", t.getMessage());
        return fallbackResult;
    }

    public String linkAccountFallback(String accountNumber, String ifscCode, String userUpiId, Throwable t) {
        log.error("ICICI linkAccount fallback active. Error: {}", t.getMessage());
        throw new BankVerificationException("ICICI Bank link account currently unavailable. Fallback error: " + t.getMessage(), t);
    }
}
