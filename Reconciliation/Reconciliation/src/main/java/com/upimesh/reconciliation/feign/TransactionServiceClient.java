package com.upimesh.reconciliation.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "transaction-service")
public interface TransactionServiceClient {

    @GetMapping("/transaction/internal/by-date")
    List<SystemTransactionDto> getTransactionsByDate(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestParam("date") String date
    );

    record SystemTransactionDto(
            String transactionId,
            String senderUpiId,
            String receiverUpiId,
            BigDecimal amount,
            String status,
            String npciTransactionId,
            String rrn,
            LocalDateTime completedAt
    ) {}
}
