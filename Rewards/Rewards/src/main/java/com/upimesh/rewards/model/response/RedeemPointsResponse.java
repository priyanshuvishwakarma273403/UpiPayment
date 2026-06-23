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
public class RedeemPointsResponse {
    private String userId;
    private int pointsRedeemed;
    private BigDecimal cashbackAmount;
    private int availablePoints;
}
