package com.paymentService.controller;

import com.paymentService.dto.request.PaymentRequest;
import com.paymentService.dto.response.PaymentResponse;
import com.paymentService.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * ================================================================
 * Payment Controller - REST API Endpoints
 * ================================================================
 * Base: /payment
 *
 * X-User-Id: API Gateway inject karta hai - current logged-in user ka ID
 *
 * Endpoints:
 * POST /payment/pay          -> Normal UPI payment
 * POST /payment/offline-pay  -> Offline queued payment
 * POST /payment/verify       -> Signature verify karo
 * GET  /payment/history      -> Payment history
 * GET  /payment/{paymentId}  -> Single payment details
 * ================================================================
 */



@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment" , description = "Payment processing APIs - UPI , QR, Offline")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * POST /payment/pay
     * Normal online UPI payment initiate karo
     */
    @PostMapping("/pay")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Initiate UPI Payment",
            description = "Initiate a real-time UPI payment. Fraud check + wallet debit happens asynchronously via Kafka."
    )
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("X-User-Id") String userIdHeader){
        Long senderId = Long.parseLong(userIdHeader);
        log.info("Payment request: senderId={}, receiver={}, amount={}",
                senderId, request.getReceiverUpiId(), request.getAmount());
        PaymentResponse response = paymentService.initiatePayment(request, senderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /payment/offline-pay
     * Offline payment queue karo (internet nahi hai tab)
     */
    @PostMapping("/offline-pay")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Queue Offline Payment",
            description = "Queue a payment when internet is unavailable. Will sync when connectivity restored."
    )
    public ResponseEntity<PaymentResponse> initiateOfflinePayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("X-User-Id") String userIdHeader){
        Long senderId = Long.parseLong(userIdHeader);
        log.info("Offline payment queued: senderId={}, amount={}", senderId, request.getAmount());

        PaymentResponse response = paymentService.initiateOfflinePayment(request, senderId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * POST /payment/verify
     * Payment signature verify karo (tamper check)
     * Body: { "paymentId": "PAY-xxx", "signature": "base64..." }
     */
    @PostMapping("/verify")
    @Operation(
            summary = "Verify Payment Signature",
            description = "Verify RSA signature of a payment to ensure it hasn't been tampered."
    )
    public ResponseEntity<Map<String, Object>> verifyPaymentSignature(
            @RequestBody Map<String, String> request){
        String paymentId = request.get("paymentId");
        String signature = request.get("signature");

        boolean valid = paymentService.verifyPayment(paymentId, signature);

        return ResponseEntity.ok(Map.of(
                "paymentId", paymentId,
                "signatureValid", valid,
                "message", valid ? "Payment signature is valid." : "Payment signature is INVALID. Possible tampering."
        ));
    }

    /**
     * GET /payment/{paymentId}
     * Ek payment ki details
     */
    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment detail by ID")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable String paymentId,
            @RequestHeader("X-User-Id") String userIdHeader){

        PaymentResponse response = paymentService.getPaymentById(paymentId, Long.parseLong(userIdHeader));
        return ResponseEntity.ok(response);

    }

    /**
     * GET /payment/history?page=0&size=10
     * Current user ki payment history (paginated)
     */
    @GetMapping("/history")
    @Operation(summary = "Get paginated payment history for current user")
    public ResponseEntity<Page<PaymentResponse>> getPaymentHistory(
            @RequestHeader("X-User-Id") String userIdHeader,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable){
        Long userId = Long.parseLong(userIdHeader);
        Page<PaymentResponse> history = paymentService.getPaymentHistory(userId, pageable);
        return ResponseEntity.ok(history);
    }

    /**
     * GET /payment/pending-sync
     * Current user ke pending offline payments
     */
    @GetMapping("/pending-sync")
    @Operation(summary = "Get pending offline payments waiting to sync")
    public ResponseEntity<java.util.List<PaymentResponse>> getPendingSyncPayments(
            @RequestHeader("X-User-Id") String userIdHeader) {

        Long userId = Long.parseLong(userIdHeader);
        return ResponseEntity.ok(paymentService.getPendingSyncPayments(userId));
    }


}
