package com.upimesh.dispute.model.request;

import com.upimesh.dispute.model.enums.DisputeReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaiseDisputeRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "User UPI ID is required")
    private String userUpiId;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotNull(message = "Dispute reason is required")
    private DisputeReason reason;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
