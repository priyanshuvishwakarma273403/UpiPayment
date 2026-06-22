package com.upimesh.reconciliation.model.response;

import com.upimesh.reconciliation.model.enums.ReconciliationStatus;
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
public class DiscrepancyResponse {

    private String discrepancyId;
    private String reportId;
    private String transactionId;
    private String bankReferenceNumber;
    private BigDecimal ourAmount;
    private BigDecimal bankAmount;
    private ReconciliationStatus status;
    private String description;
    private String resolution;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
