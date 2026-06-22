package com.upimesh.kyc.model.response;

import com.upimesh.kyc.model.enums.KycLevel;
import com.upimesh.kyc.model.enums.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycStatusResponse {
    private String kycId;
    private String userId;
    private String userUpiId;
    private KycLevel kycLevel;
    private KycStatus status;
    private boolean aadhaarVerified;
    private boolean panVerified;
    private boolean faceMatched;
    private String maskedAadhaar;
    private String maskedPan;
    private String fullName;
    private BigDecimal monthlyLimit;
}
