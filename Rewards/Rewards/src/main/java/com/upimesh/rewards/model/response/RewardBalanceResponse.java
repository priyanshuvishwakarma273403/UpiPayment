package com.upimesh.rewards.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardBalanceResponse {
    private String userId;
    private String userUpiId;
    private int totalPointsEarned;
    private int totalPointsRedeemed;
    private BigDecimal totalCashbackEarned;
    private int availablePoints;
    private String referralCode;
}
