package com.kyc_Service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Face Match Request
 * Base64 encoded selfie image
 * System automatically fetches Aadhaar photo for comparison
 */
@Data
public class FaceMatchRequest {

    @NotBlank(message = "Selfie image required")
    private String selfieBase64;    // Base64 encoded image

    private String imageFormat;     // JPEG, PNG (default: JPEG)
}
