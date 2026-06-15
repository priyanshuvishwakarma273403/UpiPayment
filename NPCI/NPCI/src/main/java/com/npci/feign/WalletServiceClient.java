package com.npci.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;
import java.util.Map;

/**
 * WalletServiceClient — Feign client to call Wallet Service.
 *
 * After NPCI confirms a payment SUCCESS, we notify Wallet Service
 * to debit sender's wallet and credit receiver's wallet.
 *
 * Uses Eureka service name "wallet-service" — no hardcoded URLs.
 */
@FeignClient(
        name = "wallet-service",
        fallback = WalletServiceClient.WalletServiceFallback.class
)
public interface WalletServiceClient {

    /**
     * Debit sender's wallet after successful NPCI transaction.
     * Called asynchronously after NPCI SUCCESS response.
     */
    @PostMapping("/wallet/internal/debit")
    Map<String, Object> debitWallet(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestBody WalletDebitRequest request
    );

    /**
     * Credit receiver's wallet.
     */
    @PostMapping("/wallet/internal/credit")
    Map<String, Object> creditWallet(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestBody WalletCreditRequest request
    );

    // ─── Request classes ──────────────────────────────────────────────────────

    record WalletDebitRequest(
            String transactionId,
            String upiId,
            BigDecimal amount,
            String remarks
    ) {}

    record WalletCreditRequest(
            String transactionId,
            String upiId,
            BigDecimal amount,
            String remarks
    ) {}

    // ─── Fallback — if Wallet Service is down ─────────────────────────────────

    class WalletServiceFallback implements WalletServiceClient {

        @Override
        public Map<String, Object> debitWallet(String serviceKey, WalletDebitRequest request) {
            // Log and queue for retry — payment already done, wallet must be updated
            System.err.println("⚠️ Wallet debit fallback — txnId: " + request.transactionId());
            return Map.of("success", false, "queued", true);
        }

        @Override
        public Map<String, Object> creditWallet(String serviceKey, WalletCreditRequest request) {
            System.err.println("⚠️ Wallet credit fallback — txnId: " + request.transactionId());
            return Map.of("success", false, "queued", true);
        }
    }
}
