package com.upimesh.settlement.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "transaction-service", url = "${transaction-service.url:http://localhost:8083}")
public interface TransactionServiceClient {

    @GetMapping("/transaction/internal/unsettled")
    List<UnsettledTransactionDto> getUnsettledTransactions(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestParam("from") String fromDate,
            @RequestParam("to") String toDate
    );

    @PostMapping("/transaction/internal/mark-settled")
    void markTransactionsSettled(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestParam("settlementId") String settlementId,
            @RequestBody List<String> txnIds
    );

    record UnsettledTransactionDto(
            String transactionId,
            String merchantUpiId,
            BigDecimal amount,
            String senderUpiId,
            LocalDateTime completedAt,
            String type
    ) {}
}
