package com.upimesh.kyc.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AadhaarOtpRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "User UPI ID is required")
    private String userUpiId;

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhaar number must be exactly 12 digits")
    private String aadhaarNumber;
}
