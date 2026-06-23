package com.upimesh.loan.model.request;

import com.upimesh.loan.model.enums.LoanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplyRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "User UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z]{2,}$", message = "Invalid UPI ID format")
    private String userUpiId;

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "100.00", message = "Minimum requested loan amount is ₹100")
    private BigDecimal requestedAmount;
}
