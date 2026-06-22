package com.upimesh.bankgateway.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceCheckRequest {

    @NotBlank(message = "Account ID is required")
    private String accountId;

    @NotBlank(message = "User UPI ID is required")
    private String userUpiId;
}
