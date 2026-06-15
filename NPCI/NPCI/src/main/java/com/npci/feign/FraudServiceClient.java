package com.npci.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;

@FeignClient(
        name = "fraud-service",
        fallback = FraudServiceClient.FraudServiceFallback.class
)
public interface FraudServiceClient {

    @PostMapping("/fraud/internal/check")
    FraudCheckResponse checkTransaction(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestBody FraudCheckRequest request
    );

    record FraudCheckRequest(
            String transactionId,
            String senderUpiId,
            String receiverUpiId,
            BigDecimal amount,
            String deviceId,
            String ipAddress,
            String transactionType
    ){}

    record FraudCheckResponse(
            boolean allowed,
            String riskLevel,       // "LOW", "MEDIUM", "HIGH"
            double riskScore,       // 0.0 to 1.0
            String blockReason      // Non-null if allowed=false
    ) {}

    /**
     * Fallback: if Fraud Service is down, allow transaction to proceed.
     * Better to allow a potentially suspicious transaction than block
     * all payments because fraud service is restarting.
     *
     * We log this for later review.
     */
    class FraudServiceFallback implements FraudServiceClient {
        @Override
        public FraudCheckResponse checkTransaction(String serviceKey, FraudCheckRequest request) {
            System.err.println(" Fraud service unavailable — allowing transaction: "
                    + request.transactionId());
            return new FraudCheckResponse(true, "UNKNOWN", 0.0, null);
        }
    }
}
