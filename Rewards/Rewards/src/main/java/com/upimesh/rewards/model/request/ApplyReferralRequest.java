package com.upimesh.rewards.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyReferralRequest {

    @NotBlank(message = "New User ID is required")
    private String newUserId;

    @NotBlank(message = "Referral code is required")
    private String referralCode;
}
