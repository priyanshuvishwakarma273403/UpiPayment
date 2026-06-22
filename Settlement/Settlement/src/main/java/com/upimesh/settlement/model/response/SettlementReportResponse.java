package com.upimesh.settlement.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementReportResponse {

    private String batchId;
    private LocalDate settlementDate;
    private int totalMerchants;
    private BigDecimal totalAmount;
    private BigDecimal processedAmount;
    private BigDecimal failedAmount;
    private double successRate;
    private List<MerchantSettlementResponse> settlements;
}
