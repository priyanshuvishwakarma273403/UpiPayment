package com.upimesh.rewards.model.response;

import com.upimesh.rewards.model.enums.RewardStatus;
import com.upimesh.rewards.model.enums.RewardType;
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
public class RewardLedgerResponse {
    private String ledgerId;
    private String userId;
    private String userUpiId;
    private String transactionId;
    private RewardType rewardType;
    private int points;
    private BigDecimal cashbackAmount;
    private RewardStatus status;
    private String description;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
