package com.upimesh.reconciliation.model.response;

import com.upimesh.reconciliation.model.enums.ReconciliationStatus;
import com.upimesh.reconciliation.model.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationSummaryResponse {

    private String reportId;
    private LocalDate date;
    private double matchRate;
    private int totalDiscrepancies;
    private BigDecimal totalDiscrepancyAmount;
    private ReportStatus status;
    private Map<ReconciliationStatus, Integer> breakdown;
}
