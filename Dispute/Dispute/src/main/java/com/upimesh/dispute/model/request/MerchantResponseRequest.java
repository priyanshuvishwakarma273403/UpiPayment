package com.upimesh.dispute.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantResponseRequest {

    @NotBlank(message = "Merchant UPI ID is required")
    private String merchantUpiId;

    @NotBlank(message = "Response is required")
    @Size(max = 500, message = "Response must not exceed 500 characters")
    private String response;

    private List<String> evidenceUrls;
}
