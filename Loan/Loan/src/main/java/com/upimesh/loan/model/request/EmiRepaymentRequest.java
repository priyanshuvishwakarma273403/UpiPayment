package com.upimesh.loan.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmiRepaymentRequest {

    @Min(value = 1, message = "EMI number must be at least 1")
    private int emiNumber;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
}
