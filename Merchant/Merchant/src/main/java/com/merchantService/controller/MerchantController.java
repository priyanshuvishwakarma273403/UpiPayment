package com.merchantService.controller;

import com.merchantService.dto.request.MerchantRegisterRequest;
import com.merchantService.dto.response.MerchantResponse;
import com.merchantService.dto.response.QrResponse;
import com.merchantService.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * ================================================================
 * Merchant Controller - REST Endpoints
 * ================================================================
 * POST /merchant/register      -> Merchant onboarding
 * GET  /merchant/qr/{merchantId} -> Static QR code
 * POST /merchant/qr/dynamic    -> Dynamic QR (amount specific)
 * GET  /merchant/{id}          -> Merchant details
 * ================================================================
 */
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Merchant", description = "Merchant onboarding and QR code APIs")
public class MerchantController {

    private final MerchantService merchantService;

    /**
     * POST /merchant/register
     * Merchant onboarding karo
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register new merchant",
            description = "Onboard a new merchant. QR code will be auto-generated.")
    public ResponseEntity<MerchantResponse> registerMerchant(
            @Valid @RequestBody MerchantRegisterRequest request) {
        log.info("Merchant registration: {}", request.getBusinessName());
        MerchantResponse response = merchantService.registerMerchant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /merchant/qr/{merchantId}
     * Merchant ka static QR code fetch karo
     */
    @GetMapping("/qr/{merchantId}")
    @Operation(summary = "Get merchant static QR code",
            description = "Returns the static QR code with base64 image for displaying/printing.")
    public ResponseEntity<QrResponse> getMerchantQr(@PathVariable Long merchantId) {
        log.info("Fetching QR for merchantId={}", merchantId);
        QrResponse response = merchantService.getMerchantQr(merchantId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /merchant/qr/dynamic
     * Dynamic QR generate karo (specific amount ke liye)
     * Body: { "merchantId": 1, "amount": 500.00, "description": "Order #123" }
     */
    @PostMapping("/qr/dynamic")
    @Operation(summary = "Generate dynamic QR code",
            description = "Generate per-transaction QR with specific amount. Expires in 15 minutes.")
    public ResponseEntity<QrResponse> generateDynamicQr(
            @RequestBody Map<String, Object> request) {
        Long merchantId = Long.valueOf(request.get("merchantId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String description = (String) request.getOrDefault("description", "Payment");
        QrResponse response = merchantService.generateDynamicQr(merchantId, amount, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /merchant/{id}
     * Merchant details fetch karo
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get merchant by ID")
    public ResponseEntity<MerchantResponse> getMerchant(@PathVariable Long id) {
        return ResponseEntity.ok(merchantService.getMerchantById(id));
    }
}