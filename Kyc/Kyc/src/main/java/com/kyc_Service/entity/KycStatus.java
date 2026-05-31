package com.kyc_Service.entity;


public enum KycStatus {
    PENDING,
    AADHAAR_OTP_SENT,
    AADHAAR_VERIFIED,
    PAN_SUBMITTED,
    PAN_VERIFIED,
    FACE_MATCH_PENDING,
    COMPLETED,
    FAILED,
    REJECTED,
    EXPIRED
}
