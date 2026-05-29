package com.walletService.dto.response;

import com.walletService.entity.Wallet;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Wallet Response DTO
 * Client ko wallet info is format mein milegi
 */

@Data
@Builder
public class WalletResponse {

    private Long walletId;
    private Long userId;
    private String upiId;
    private BigDecimal balance;
    private BigDecimal availableBalance;  // balance - frozenAmount
    private BigDecimal frozenAmount;
    private BigDecimal dailyLimit;
    private Boolean isActive;
    private LocalDateTime lastUpdated;

    // Entity se DTO banao (static factory method)
    public static WalletResponse fromEntity(Wallet wallet) {
        return WalletResponse.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUserId())
                .upiId(wallet.getUpiId())
                .balance(wallet.getBalance())
                .availableBalance(wallet.getAvailableBalance())
                .frozenAmount(wallet.getFrozenAmount())
                .dailyLimit(wallet.getDailyLimit())
                .isActive(wallet.getIsActive())
                .lastUpdated(wallet.getUpdatedAt())
                .build();
    }

}
