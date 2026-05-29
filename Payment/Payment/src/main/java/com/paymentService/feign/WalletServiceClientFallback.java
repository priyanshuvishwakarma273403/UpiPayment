package com.paymentService.feign;

import com.paymentService.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Wallet Service Feign Client Fallback
 * Jab wallet-service unreachable hoga, yeh methods call honge.
 */

@Component
@Slf4j
public class WalletServiceClientFallback implements WalletServiceClient{


    @Override
    public Map<String, Object> getBalance(Long userId) {
        log.error("Fallback: wallet-service unavailable for getBalance userId={}", userId);
        return Map.of("error", "WALLET_SERVICE_UNAVAILABLE", "availableBalance", 0);
    }

    @Override
    public Map<String, Object> debitWallet(WalletDebitCreditRequest request) {
        log.error("Fallback: wallet-service unavailable for debit userId={}", request.getUserId());
        throw new PaymentException(
                "Wallet service unavailable. Payment will be queued.");
    }

    @Override
    public Map<String, Object> creditWallet(WalletDebitCreditRequest request) {
        log.error("Fallback: wallet-service unavailable for credit userId={}", request.getUserId());
        return Map.of("error", "WALLET_SERVICE_UNAVAILABLE", "success", false);
    }

    @Override
    public Map<String, Object> freezeAmount(WalletDebitCreditRequest request) {
        log.error("Fallback: wallet-service unavailable for freeze userId={}", request.getUserId());
        throw new PaymentException(
                "Wallet service unavailable. Cannot freeze amount.");
    }

    @Override
    public Map<String, Object> releaseAmount(WalletDebitCreditRequest request) {
        log.error("Fallback: wallet-service unavailable for release userId={}", request.getUserId());
        return Map.of("error", "WALLET_SERVICE_UNAVAILABLE", "success", false);
    }
}
