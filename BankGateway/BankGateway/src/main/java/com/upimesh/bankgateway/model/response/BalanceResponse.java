package com.upimesh.bankgateway.model.response;

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
public class BalanceResponse {

    private String accountId;
    private String maskedAccountNumber;
    private String bankName;
    private BigDecimal availableBalance;
    private BigDecimal ledgerBalance;

    @Builder.Default
    private String currency = "INR";
    
    private LocalDateTime fetchedAt;
    private boolean fromCache;
}
