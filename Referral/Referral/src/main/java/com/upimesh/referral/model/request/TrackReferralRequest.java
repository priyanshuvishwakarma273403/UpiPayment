package com.upimesh.referral.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackReferralRequest {

    @NotBlank(message = "Referral code is required")
    @Size(min = 10, max = 10, message = "Referral code must be exactly 10 characters")
    private String referralCode;

    @NotBlank(message = "Referee User ID is required")
    private String refereeId;

    @NotBlank(message = "Referee UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z]{2,}$", message = "Invalid UPI ID format")
    private String refereeUpiId;

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "IP Address is required")
    private String ipAddress;
}
