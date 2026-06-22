package com.upimesh.reconciliation.model.response;

import com.upimesh.reconciliation.model.enums.ReportStatus;
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
public class ReconciliationReportResponse {

    private String reportId;
    private LocalDate reconciliationDate;
    private int totalSystemTransactions;
    private int totalBankTransactions;
    private int matchedCount;
    private int mismatchCount;
    private int missingInBankCount;
    private int missingInSystemCount;
    private int duplicateCount;
    private BigDecimal totalSystemAmount;
    private BigDecimal totalBankAmount;
    private BigDecimal discrepancyAmount;
    private ReportStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
