package com.upimesh.settlement.model.response;

import com.upimesh.settlement.model.enums.SettlementMode;
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
public class MerchantSettlementResponse {

    private String settlementId;
    private String batchId;
    private String merchantUpiId;
    private String maskedBankAccount;
    private String merchantIfsc;
    private String merchantBankName;
    private int transactionCount;
    private BigDecimal grossAmount;
    private BigDecimal platformFee;
    private BigDecimal gstOnFee;
    private BigDecimal netAmount;
    private SettlementMode mode;
    private SettlementStatus status;
    private String bankReferenceNumber;
    private String failureReason;
    private LocalDate settlementDate;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}
