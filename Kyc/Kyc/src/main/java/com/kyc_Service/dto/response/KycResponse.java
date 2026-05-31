package com.kyc_Service.dto.response;

import com.kyc_Service.entity.KycLevel;
import com.kyc_Service.entity.KycRecord;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * KYC Response DTO - user ko yeh milega
 */
@Data
@Builder
public class KycResponse {

    private Long userId;
    private String kycLevel;
    private String kycStatus;
    private String aadhaarMasked;       // XXXX-XXXX-1234
    private String panMasked;           // XXXXX1234X
    private String fullName;
    private String gender;
    private Boolean aadhaarVerified;
    private Boolean panVerified;
    private Boolean faceMatched;
    private Double faceMatchScore;
    private LocalDateTime verifiedAt;
    private LocalDateTime expiresAt;
    private String message;
    private String nextStep;            // User ko kya karna hai next

    // Monthly transaction limits based on KYC level
    private String monthlyLimit;

    public static KycResponse fromEntity(KycRecord record) {
        return KycResponse.builder()
                .userId(record.getUserId())
                .kycLevel(record.getKycLevel().name())
                .kycStatus(record.getKycStatus().name())
                .aadhaarMasked(record.getAadhaarMasked())
                .panMasked(maskPan(record.getPanNumber()))
                .fullName(record.getFullName())
                .gender(record.getGender())
                .aadhaarVerified(record.getAadhaarVerifiedAt() != null)
                .panVerified(record.getPanVerifiedAt() != null)
                .faceMatched(record.getFaceMatchedAt() != null)
                .faceMatchScore(record.getFaceMatchScore())
                .verifiedAt(record.getVerifiedAt())
                .expiresAt(record.getExpiresAt())
                .monthlyLimit(getMonthlyLimit(record.getKycLevel()))
                .build();
    }

    private static String maskPan(String pan) {
        if (pan == null || pan.length() < 10) return null;
        return "XXXXX" + pan.substring(5, 9) + pan.charAt(9);
    }

    private static String getMonthlyLimit(KycLevel level) {
        return switch (level) {
            case LEVEL_0 -> "₹10,000 per month";
            case LEVEL_1 -> "₹1,00,000 per month";
            case LEVEL_2 -> "Unlimited";
        };
    }

}
