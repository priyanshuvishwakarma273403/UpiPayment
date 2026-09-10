package com.paymentService.feign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;

@FeignClient(
        name = "aml-service",
        fallback = AmlServiceClient.AmlFallback.class
)
public interface AmlServiceClient {

    @PostMapping("/aml/screen")
    AmlScreeningResponse screenTransaction(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @RequestBody AmlScreeningRequest request
    );

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AmlScreeningRequest(
            String transactionId,
            String senderUpiId,
            String receiverUpiId,
            String senderName,
            BigDecimal amount,
            String ipAddress
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AmlScreeningResponse(
            String transactionId,
            boolean allowed,
            String status,
            String reasons
    ) {}

    class AmlFallback implements AmlServiceClient {
        @Override
        public AmlScreeningResponse screenTransaction(String serviceKey, AmlScreeningRequest request) {
            System.err.println("AML service fallback called for txn: " + request.transactionId());
            return new AmlScreeningResponse(request.transactionId(), true, "PASSED", null);
        }
    }
}
