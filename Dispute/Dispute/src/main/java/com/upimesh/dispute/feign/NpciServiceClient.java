package com.upimesh.dispute.feign;

import com.upimesh.dispute.feign.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "npci-integration-service")
public interface NpciServiceClient {

    @GetMapping("/npci/check-status/{transactionId}")
    NpciApiResponse<TransactionResponse> checkStatus(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @PathVariable("transactionId") String transactionId
    );

    @PostMapping("/npci/refund")
    NpciApiResponse<RefundResponse> initiateRefund(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestBody RefundRequest request
    );
}
