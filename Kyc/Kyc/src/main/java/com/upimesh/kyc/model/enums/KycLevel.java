package com.upimesh.kyc.model.enums;

public enum KycLevel {
    LEVEL_0, // Phone + OTP verification
    LEVEL_1, // PAN + Aadhaar OTP verification
    LEVEL_2  // Full KYC (video/biometrics/face match)
}
