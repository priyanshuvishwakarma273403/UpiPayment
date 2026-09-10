package com.npci.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@FeignClient(
        name = "fraud-service",
        fallback = FraudServiceClient.FraudServiceFallback.class
)
public interface FraudServiceClient {

    @PostMapping("/fraud/check")
    FraudCheckResponse checkTransaction(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @RequestBody FraudCheckRequest request
    );

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FraudCheckRequest(
            String paymentId,
            Long senderId,
            Long receiverId,
            String senderUpiId,
            String receiverUpiId,
            BigDecimal amount,
            String paymentMode,
            String deviceId,
            String ipAddress
    ){
        public FraudCheckRequest(String transactionId, String senderUpiId, String receiverUpiId, BigDecimal amount, String deviceId, String ipAddress, String transactionType) {
            this(transactionId, 1L, null, senderUpiId, receiverUpiId, amount, transactionType, deviceId, ipAddress);
        }
        public String transactionId() { return paymentId; }
        public String transactionType() { return paymentMode; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FraudCheckResponse(
            Boolean allowed,
            String riskLevel,
            Double riskScore,
            String blockReason,
            String decision,
            String reasons
    ) {
        public Boolean allowed() {
            return allowed;
        }

        public boolean isAllowed() {
            if (allowed != null) return allowed;
            if (decision != null) return !"BLOCKED".equalsIgnoreCase(decision);
            return true;
        }

        public String getBlockReason() {
            if (blockReason != null) return blockReason;
            if (reasons != null) return reasons;
            return "Transaction blocked by fraud rules";
        }
    }

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
            return new FraudCheckResponse(true, "UNKNOWN", 0.0, null, "SAFE", null);
        }
    }
}
