package com.paymentService.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

//**
//        * ================================================================
//        * Wallet Service Feign Client
// * ================================================================
//         * OpenFeign: Ek interface define karo, Spring automatically
// * HTTP client banata hai.
//        *
//        * name = "wallet-service": Eureka mein registered service ka naam.
//        * Load balancing automatic hoga.
//        *
//        * Circuit Breaker + Retry:
//        * application.yml mein Resilience4j config hai.
// * Agar wallet-service down ho, fallback method chalega.
// * ================================================================
//         */
@FeignClient(
        name = "wallet-service",
        fallback = WalletServiceClientFallback.class
)
public interface WalletServiceClient {

    @GetMapping("/wallet/balance/{userId}")
    Map<String, Object> getBalance(@PathVariable Long userId);

    @PostMapping("/wallet/debit")
    Map<String, Object> debitWallet(@RequestBody WalletDebitCreditRequest request);

    @PostMapping("/wallet/credit")
    Map<String, Object> creditWallet(@RequestBody WalletDebitCreditRequest request);

    @PostMapping("/wallet/freeze")
    Map<String, Object> freezeAmount(@RequestBody WalletDebitCreditRequest request);

    @PostMapping("/wallet/release")
    Map<String, Object> releaseAmount(@RequestBody WalletDebitCreditRequest request);
}
