package com.kyc_Service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Aadhaar OTP verify karne ke liye */
@Data
public class AadhaarVerifyRequest {

    @NotBlank
    @Pattern(regexp = "^[2-9]{1}[0-9]{11}$", message = "Valid 12-digit Aadhaar required")
    private String aadhaarNumber;

    @NotBlank(message = "OTP required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;

    private String txnId;  // UIDAI transaction ID from OTP initiation

}
