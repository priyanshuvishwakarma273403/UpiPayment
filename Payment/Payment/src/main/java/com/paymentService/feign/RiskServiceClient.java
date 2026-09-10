package com.paymentService.feign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;

@FeignClient(
        name = "risk-scoring-service",
        fallback = RiskServiceClient.RiskFallback.class
)
public interface RiskServiceClient {

    @PostMapping("/risk/score")
    RiskScoringResponse scoreTransaction(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @RequestBody RiskScoringRequest request
    );

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RiskScoringRequest(
            String transactionId,
            String userId,
            String senderUpiId,
            String receiverUpiId,
            BigDecimal amount,
            String deviceId,
            String ipAddress,
            String location,
            String transactionType
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RiskScoringResponse(
            String transactionId,
            String userId,
            double finalScore,
            String riskLevel,
            boolean allowed,
            String actionRecommended
    ) {}

    class RiskFallback implements RiskServiceClient {
        @Override
        public RiskScoringResponse scoreTransaction(String serviceKey, RiskScoringRequest request) {
            System.err.println("Risk service fallback called for txn: " + request.transactionId());
            return new RiskScoringResponse(request.transactionId(), request.userId(), 0.0, "LOW", true, "ALLOW");
        }
    }
}
