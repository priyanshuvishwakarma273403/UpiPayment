package com.transaction_service.controller;

import com.transaction_service.dto.response.TransactionResponse;
import com.transaction_service.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction history and ledger APIs")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * GET /transactions/user/{id}
     * User ki paginated transaction history
     */
    @GetMapping("/user/{id}")
    @Operation(summary = "Get user transaction history")
    public ResponseEntity<Page<TransactionResponse>> getUserTransactions(
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(transactionService.getUserTransactions(id, pageable));
    }

    /**
     * GET /transactions/merchant/{id}
     * Merchant ki transaction history + settlement data
     */
    @GetMapping("/merchant/{id}")
    @Operation(summary = "Get merchant transaction history")
    public ResponseEntity<Page<TransactionResponse>> getMerchantTransactions(
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(transactionService.getMerchantTransactions(id, pageable));
    }

    /**
     * GET /transactions/spend-summary/{userId}?days=30
     * Total spend summary for a user
     */
    @GetMapping("/spend-summary/{userId}")
    @Operation(summary = "Get total spend summary for user")
    public ResponseEntity<Map<String, Object>> getSpendSummary(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        BigDecimal total = transactionService.getTotalSpend(userId, days);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "days", days,
                "totalSpend", total,
                "currency", "INR"
        ));
    }

    /**
     * GET /transactions/debit-between
     * Query debit transactions between date range for analytics aggregation
     */
    @GetMapping("/debit-between")
    @Operation(summary = "Get all debit transactions between two timestamps")
    public ResponseEntity<java.util.List<TransactionResponse>> getDebitTransactionsBetween(
            @RequestParam("from") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime from,
            @RequestParam("to") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime to) {
        return ResponseEntity.ok(transactionService.getDebitTransactionsBetween(from, to));
    }
}
