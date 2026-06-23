package com.upimesh.subscription.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateTransactionRequest {
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private String remarks;
    private TransactionType type;
    private String deviceId;
    private String ipAddress;
    private String mpinHash;
    private String idempotencyKey;
}
