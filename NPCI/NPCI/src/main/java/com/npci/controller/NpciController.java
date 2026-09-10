package com.npci.controller;

import com.npci.model.request.InitiateTransactionRequest;
import com.npci.model.request.MandateCreateRequest;
import com.npci.model.request.RefundRequest;
import com.npci.model.response.ApiResponse;
import com.npci.model.response.MandateResponse;
import com.npci.model.response.RefundResponse;
import com.npci.model.response.TransactionResponse;
import com.npci.service.MandateService;
import com.npci.service.NpciTransactionService;
import com.npci.service.RefundService;
import jakarta.transaction.InvalidTransactionException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//**
//        * NpciController — All NPCI endpoints.
// *
//         * Base path: /npci
// * All routes go through API Gateway which validates JWT before reaching here.
//        *
//        * Endpoints:
//        *   POST /npci/initiate-transaction   → Initiate UPI payment
// *   GET  /npci/check-status/{txnId}   → Get transaction status
// *   POST /npci/refund                 → Initiate refund
// *   GET  /npci/refund/{refundId}      → Get refund status
// *   POST /npci/mandate-create         → Create recurring mandate
// *   PUT  /npci/mandate/{id}/pause     → Pause mandate
// *   PUT  /npci/mandate/{id}/revoke    → Revoke/cancel mandate
// *   GET  /npci/mandate/user/{upiId}   → Get user's active mandates
//        */
@RestController
@RequestMapping("/npci")
@RequiredArgsConstructor
@Slf4j
public class NpciController {

    private final NpciTransactionService transactionService;
    private final RefundService refundService;
    private final MandateService mandateService;

    // ─── Transaction Endpoints ────────────────────────────────────────────────

    /**
     * POST /npci/initiate-transaction
     *
     * Main payment endpoint. Called by Payment Service when user clicks "Pay".
     * Validates request, checks limits, calls NPCI, returns transaction status.
     */
    @PostMapping("/initiate-transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> initiateTransaction(
            @Valid @RequestBody InitiateTransactionRequest request) throws InvalidTransactionException {

        log.info("Initiating transaction | sender={} | receiver={} | amount={}",
                request.getSenderUpiId(), request.getReceiverUpiId(), request.getAmount());

        TransactionResponse response = transactionService.initiateTransaction(request);

        return ResponseEntity.ok(ApiResponse.success(response,
                "Transaction " + response.getStatus().name().toLowerCase()));
    }

    /**
     * GET /npci/check-status/{transactionId}
     *
     * Check payment status. Frontend polls this after initiating payment.
     * Also polls NPCI for latest status if transaction is still PENDING.
     */
    @GetMapping("/check-status/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> checkStatus(
            @PathVariable String transactionId) {

        log.info("Checking transaction status | txnId={}", transactionId);
        TransactionResponse response = transactionService.checkStatus(transactionId);

        return ResponseEntity.ok(ApiResponse.success(response, "Status fetched"));
    }

    // ─── Refund Endpoints ─────────────────────────────────────────────────────

    /**
     * POST /npci/refund
     *
     * Initiate refund for a successful transaction.
     * Supports partial refunds (refundAmount <= original amount).
     * Called by Dispute Service or Merchant Service.
     */
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<RefundResponse>> initiateRefund(
            @Valid @RequestBody RefundRequest request) throws InvalidTransactionException {

        log.info("Initiating refund | originalTxn={} | amount={}",
                request.getOriginalTransactionId(), request.getRefundAmount());

        RefundResponse response = refundService.initiateRefund(request);

        return ResponseEntity.ok(ApiResponse.success(response,
                "Refund " + response.getStatus().name().toLowerCase() +
                        ". Expected credit: " + response.getExpectedCreditBy()));
    }

    /**
     * GET /npci/refund/{refundId}
     *
     * Check refund status. Refunds typically take T+1 to T+3 days.
     */
    @GetMapping("/refund/{refundId}")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefundStatus(
            @PathVariable String refundId) {

        RefundResponse response = refundService.getRefundStatus(refundId);
        return ResponseEntity.ok(ApiResponse.success(response, "Refund status fetched"));
    }

    // ─── Mandate Endpoints ────────────────────────────────────────────────────

    /**
     * POST /npci/mandate-create
     *
     * Create a UPI AutoPay recurring mandate.
     * User will receive OTP/notification for consent.
     * Once approved, auto-debits happen on schedule.
     */
    @PostMapping("/mandate-create")
    public ResponseEntity<ApiResponse<MandateResponse>> createMandate(
            @Valid @RequestBody MandateCreateRequest request) {

        log.info("Creating mandate | user={} | merchant={} | maxAmount={}",
                request.getUserUpiId(), request.getMerchantName(), request.getMaxAmount());

        MandateResponse response = mandateService.createMandate(request);

        return ResponseEntity.ok(ApiResponse.success(response,
                "Mandate created. Awaiting user approval."));
    }

    /**
     * PUT /npci/mandate/{mandateId}/pause
     *
     * Temporarily pause an active mandate.
     * No auto-debits will happen while paused.
     */
    @PutMapping("/mandate/{mandateId}/pause")
    public ResponseEntity<ApiResponse<MandateResponse>> pauseMandate(
            @PathVariable String mandateId) throws InvalidTransactionException {

        MandateResponse response = mandateService.pauseMandate(mandateId);
        return ResponseEntity.ok(ApiResponse.success(response, "Mandate paused successfully"));
    }

    /**
     * PUT /npci/mandate/{mandateId}/revoke
     *
     * Permanently cancel a mandate.
     * Cannot be undone — user must create a new mandate.
     */
    @PutMapping("/mandate/{mandateId}/revoke")
    public ResponseEntity<ApiResponse<MandateResponse>> revokeMandate(
            @PathVariable String mandateId) throws InvalidTransactionException {

        MandateResponse response = mandateService.revokeMandate(mandateId);
        return ResponseEntity.ok(ApiResponse.success(response, "Mandate cancelled successfully"));
    }

    /**
     * GET /npci/mandate/user/{userUpiId}
     *
     * Get all active mandates for a user.
     * Used in frontend to show "Manage AutoPay" section.
     */
    @GetMapping("/mandate/user/{userUpiId}")
    public ResponseEntity<ApiResponse<List<MandateResponse>>> getUserMandates(
            @PathVariable String userUpiId) {

        List<MandateResponse> mandates = mandateService.getUserMandates(userUpiId);
        return ResponseEntity.ok(ApiResponse.success(mandates,
                "Found " + mandates.size() + " active mandates"));
    }
}
