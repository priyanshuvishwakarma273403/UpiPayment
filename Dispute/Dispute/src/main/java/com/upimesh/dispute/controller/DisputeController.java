package com.upimesh.dispute.controller;

import com.upimesh.dispute.model.request.MerchantResponseRequest;
import com.upimesh.dispute.model.request.RaiseDisputeRequest;
import com.upimesh.dispute.model.request.ResolveDisputeRequest;
import com.upimesh.dispute.model.response.ApiResponse;
import com.upimesh.dispute.model.response.DisputeResponse;
import com.upimesh.dispute.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dispute")
@RequiredArgsConstructor
@Slf4j
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping("/raise")
    public ResponseEntity<ApiResponse<DisputeResponse>> raiseDispute(
            @Valid @RequestBody RaiseDisputeRequest request) {
        log.info("REST request to raise dispute for transactionId: {}", request.getTransactionId());
        DisputeResponse response = disputeService.raiseDispute(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Dispute raised and merchant notified successfully"));
    }

    @GetMapping("/{disputeId}")
    public ResponseEntity<ApiResponse<DisputeResponse>> getDispute(
            @PathVariable String disputeId) {
        log.info("REST request to get dispute: {}", disputeId);
        DisputeResponse response = disputeService.getDispute(disputeId);
        return ResponseEntity.ok(ApiResponse.success(response, "Dispute fetched successfully"));
    }

    @PostMapping("/{disputeId}/merchant-response")
    public ResponseEntity<ApiResponse<DisputeResponse>> submitMerchantResponse(
            @PathVariable String disputeId,
            @Valid @RequestBody MerchantResponseRequest request) {
        log.info("REST request from merchant to respond to dispute: {}", disputeId);
        DisputeResponse response = disputeService.submitMerchantResponse(disputeId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Merchant response recorded successfully"));
    }

    @PutMapping("/{disputeId}/resolve")
    public ResponseEntity<ApiResponse<DisputeResponse>> resolveDispute(
            @PathVariable String disputeId,
            @Valid @RequestBody ResolveDisputeRequest request) {
        log.info("REST request to resolve dispute: {}", disputeId);
        DisputeResponse response = disputeService.resolveDispute(disputeId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Dispute resolved successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<DisputeResponse>>> getDisputesByUser(
            @PathVariable String userId) {
        log.info("REST request to get disputes raised by user: {}", userId);
        List<DisputeResponse> responses = disputeService.getDisputesByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(responses, "User disputes fetched successfully"));
    }

    @GetMapping("/merchant/{merchantUpiId}")
    public ResponseEntity<ApiResponse<List<DisputeResponse>>> getDisputesByMerchant(
            @PathVariable String merchantUpiId) {
        log.info("REST request to get disputes received by merchant: {}", merchantUpiId);
        List<DisputeResponse> responses = disputeService.getDisputesByMerchant(merchantUpiId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Merchant disputes fetched successfully"));
    }

    @PostMapping("/check-expired")
    public ResponseEntity<ApiResponse<Void>> checkExpiredMerchantResponses() {
        log.info("REST request to manually check expired merchant responses");
        disputeService.checkExpiredMerchantResponses();
        return ResponseEntity.ok(ApiResponse.success(null, "Expired disputes processed successfully"));
    }
}
