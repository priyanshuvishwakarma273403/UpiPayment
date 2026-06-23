package com.upimesh.dispute.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveDisputeRequest {

    @NotBlank(message = "Resolution beneficiary is required")
    @Pattern(regexp = "^(USER|MERCHANT)$", message = "Must be resolved in favor of either 'USER' or 'MERCHANT'")
    private String inFavorOf;

    @NotBlank(message = "Resolution notes are required")
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @NotBlank(message = "Resolved by (Admin username) is required")
    private String resolvedBy;
}
