package com.upimesh.payroll.feign;

import com.upimesh.payroll.feign.dto.BankApiResponse;
import com.upimesh.payroll.feign.dto.UpiResolveResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "bank-gateway-service")
public interface BankGatewayServiceClient {

    @GetMapping("/bank/upi/resolve/{upiHandle}")
    BankApiResponse<UpiResolveResponse> resolveUpiHandle(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @PathVariable("upiHandle") String upiHandle
    );
}
