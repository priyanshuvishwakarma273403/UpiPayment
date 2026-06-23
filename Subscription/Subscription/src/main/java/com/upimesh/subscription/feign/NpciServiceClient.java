package com.upimesh.subscription.feign;

import com.upimesh.subscription.feign.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "npci-integration-service")
public interface NpciServiceClient {

    @PostMapping("/npci/mandate-create")
    NpciApiResponse<MandateResponse> createMandate(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestBody MandateCreateRequest request
    );

    @PutMapping("/npci/mandate/{mandateId}/pause")
    NpciApiResponse<MandateResponse> pauseMandate(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @PathVariable("mandateId") String mandateId
    );

    @PutMapping("/npci/mandate/{mandateId}/revoke")
    NpciApiResponse<MandateResponse> revokeMandate(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @PathVariable("mandateId") String mandateId
    );

    @PostMapping("/npci/initiate-transaction")
    NpciApiResponse<TransactionResponse> initiateTransaction(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestBody InitiateTransactionRequest request
    );
}
