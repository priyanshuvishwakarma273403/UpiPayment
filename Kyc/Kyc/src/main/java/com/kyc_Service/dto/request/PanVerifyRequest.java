package com.kyc_Service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** PAN Card verification request */
@Data
public class PanVerifyRequest {

    @NotBlank(message = "PAN number required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Valid PAN format: ABCDE1234F")
    private String panNumber;

    @NotBlank(message = "Full name required as on PAN")
    private String nameAsOnPan;

    // Date of birth for cross-verification
    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "DOB format: DD/MM/YYYY")
    private String dateOfBirth;

}
