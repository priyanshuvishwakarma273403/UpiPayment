package com.upimesh.analytics.dto;

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
public class TransactionResponseDto {
    private Long id;
    private String paymentId;
    private Long userId;
    private Long merchantId;
    private Long counterPartyId;
    private String counterPartyUpiId;
    private BigDecimal amount;
    private String transactionType;
    private String paymentMode;
    private String status;
    private String description;
    private String referenceNumber;
    private LocalDateTime createdAt;
}
