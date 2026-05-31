package com.kyc_Service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** Aadhaar OTP initiate karne ke liye */
@Data
public class AadhaarOtpRequest {

    @NotBlank(message = "Aadhaar number required")
    @Pattern(regexp = "^[2-9]{1}[0-9]{11}$", message = "Valid 12-digit Aadhaar number required")
    private String aadhaarNumber;

}
