package com.upimesh.reconciliation.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "npci-integration-service", url = "${npci-integration-service.url:http://localhost:8086}")
public interface NpciServiceClient {

    @GetMapping("/npci/internal/status/{transactionId}")
    Map<String, Object> getTransactionStatus(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @PathVariable("transactionId") String transactionId
    );
}
