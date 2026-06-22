package com.upimesh.kyc.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AadhaarOtpResponse {
    private String kycId;
    private String message;
    private int expirySeconds;
}
