package com.upimesh.subscription.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String transactionId;
    private String npciTransactionId;
    private String rrn;
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private TransactionStatus status;
    private TransactionType type;
    private String remarks;
    private String npciResponseCode;
    private String npciResponseMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
