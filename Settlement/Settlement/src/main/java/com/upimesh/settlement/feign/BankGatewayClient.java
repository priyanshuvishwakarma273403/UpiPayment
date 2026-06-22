package com.upimesh.settlement.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "bank-gateway-service", url = "${bank-gateway-service.url:http://localhost:8091}")
public interface BankGatewayClient {

    @GetMapping("/bank/accounts/primary/{merchantUpiId}")
    MerchantBankDetailsDto getMerchantBankDetails(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @PathVariable("merchantUpiId") String merchantUpiId
    );

    record MerchantBankDetailsDto(
            String accountId,
            String encryptedAccountNumber,
            String maskedAccountNumber,
            String ifscCode,
            String bankName,
            String bankReferenceToken
    ) {}
}
