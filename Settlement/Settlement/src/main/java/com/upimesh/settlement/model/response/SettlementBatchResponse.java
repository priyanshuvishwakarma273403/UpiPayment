package com.upimesh.settlement.model.response;

import com.upimesh.settlement.model.enums.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementBatchResponse {

    private String batchId;
    private LocalDate settlementDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalMerchants;
    private int totalTransactions;
    private BigDecimal totalAmount;
    private BigDecimal processedAmount;
    private BigDecimal failedAmount;
    private SettlementStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
