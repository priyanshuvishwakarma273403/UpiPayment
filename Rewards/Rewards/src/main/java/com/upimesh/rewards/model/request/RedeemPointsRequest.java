package com.upimesh.rewards.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedeemPointsRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Points to redeem is required")
    @Min(value = 1, message = "Minimum points to redeem is 1")
    private Integer pointsToRedeem;
}
