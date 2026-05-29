package com.walletService.controller;

import com.walletService.dto.request.AddMoneyRequest;
import com.walletService.dto.request.DebitCreditRequest;
import com.walletService.dto.response.WalletResponse;
import com.walletService.entity.Wallet;
import com.walletService.service.WalletService;
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
 * Wallet Controller - REST Endpoints
 * ================================================================
 * Base path: /wallet
 *
 * X-User-Id header: API Gateway JWT filter se inject hota hai
 * Yeh header downstream services ko user info deta hai
 * bina dobara JWT validate kiye.
 * ================================================================
 */
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Wallet" , description = "Wallet management APIs")
public class WalletController {

    private final WalletService walletService;

    /**
     * GET /wallet/balance/{userId}
     * User ka available balance fetch karo (Redis cached)
     */
    @GetMapping("/balance/{userId}")
    @Operation(summary = "Get wallet balance", description = "Returns available balance (cached in redis)")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable Long userId){
        BigDecimal balance = walletService.getBalance(userId);
        return ResponseEntity.ok(Map.of(
                "userId",  userId,
                "availableBalance", balance,
                "currency" , "INR"
        ));
    }

    /**
     * POST /wallet/create
     * Naya wallet create karo (auth-service register ke baad Kafka event se call hoga)
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create wallet ", description = "Create new wallet for a user")
    public ResponseEntity<WalletResponse> createWallet(
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestBody Map<String, String>  body){

        Long userId = Long.parseLong(userIdHeader);
        String upiId = body.get("upiId");

        Wallet wallet = walletService.createWallet(userId, upiId);
        return ResponseEntity.status(HttpStatus.CREATED).body(WalletResponse.fromEntity(wallet));

    }

    /**
     * POST /wallet/add-money
     * Wallet mein paisa add karo (bank se top-up)
     */
    @PostMapping("/add-money")
    @Operation(summary = "Add money to wallet ", description = "Top-up wallet from bank account")
    public ResponseEntity<WalletResponse> addMoney(@Valid @RequestBody AddMoneyRequest request){
        log.info("Add money request: userId={}, amount={}", request.getUserId(), request.getAmount());
        Wallet wallet = walletService.addMoney(
                request.getUserId(),
                request.getAmount(),
                request.getBankReference());
        return ResponseEntity.ok(WalletResponse.fromEntity(wallet));

    }

    /**
     * POST /wallet/debit
     * Wallet se paisa kato (payment-service yeh call karta hai)
     */
    @PostMapping("/debit")
    @Operation(summary = "Debit wallet", description = "Debit amount from wallet for a payment")
    public ResponseEntity<Map<String, Object>> debitWallet(
            @Valid @RequestBody DebitCreditRequest request) {

        log.info("Debit request: userId={}, amount={}, paymentId={}",
                request.getUserId(), request.getAmount(), request.getPaymentId());

        walletService.debitWallet(
                request.getUserId(),
                request.getAmount(),
                request.getPaymentId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Wallet debited successfully",
                "paymentId", request.getPaymentId(),
                "amount", request.getAmount()
        ));
    }

    /**
     * POST /wallet/credit
     * Wallet mein paisa daalo (payment receive karne par)
     */
    @PostMapping("/credit")
    @Operation(summary = "Credit wallet", description = "Credit amount to wallet after receiving payment")
    public ResponseEntity<Map<String, Object>> creditWallet(
            @Valid @RequestBody DebitCreditRequest request) {

        log.info("Credit request: userId={}, amount={}, paymentId={}",
                request.getUserId(), request.getAmount(), request.getPaymentId());

        walletService.creditWallet(
                request.getUserId(),
                request.getAmount(),
                request.getPaymentId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Wallet credited successfully",
                "paymentId", request.getPaymentId(),
                "amount", request.getAmount()
        ));
    }

    /**
     * GET /wallet/info/{userId}
     * Full wallet details
     */
    @GetMapping("/info/{userId}")
    public ResponseEntity<WalletResponse> getWalletInfo(@PathVariable Long userId) {
        BigDecimal balance = walletService.getBalance(userId);
        // Simplified response - full wallet fetch alag se
        return ResponseEntity.ok(WalletResponse.builder()
                .userId(userId)
                .balance(balance)
                .availableBalance(balance)
                .build());
    }

}
