package com.transaction_service.dto.response;

import com.transaction_service.entity.mysql.Transaction;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {

    private Long id;
    private String paymentId;
    private Long userId;
    private Long counterPartyId;
    private String counterPartyUpiId;
    private BigDecimal amount;
    private String transactionType;
    private String paymentMode;
    private String status;
    private String description;
    private String referenceNumber;
    private LocalDateTime createdAt;

    public static TransactionResponse fromEntity(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .paymentId(t.getPaymentId())
                .userId(t.getUserId())
                .counterPartyId(t.getCounterPartyId())
                .counterPartyUpiId(t.getCounterPartyUpiId())
                .amount(t.getAmount())
                .transactionType(t.getTransactionType().name())
                .paymentMode(t.getPaymentMode() != null ? t.getPaymentMode().name() : null)
                .status(t.getStatus().name())
                .description(t.getDescription())
                .referenceNumber(t.getReferenceNumber())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
