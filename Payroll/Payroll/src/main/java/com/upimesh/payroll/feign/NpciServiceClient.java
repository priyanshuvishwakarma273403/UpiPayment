package com.upimesh.payroll.feign;

import com.upimesh.payroll.feign.dto.InitiateTransactionRequest;
import com.upimesh.payroll.feign.dto.NpciApiResponse;
import com.upimesh.payroll.feign.dto.TransactionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "npci-integration-service")
public interface NpciServiceClient {

    @PostMapping("/npci/initiate-transaction")
    NpciApiResponse<TransactionResponse> initiateTransaction(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestBody InitiateTransactionRequest request
    );
}
