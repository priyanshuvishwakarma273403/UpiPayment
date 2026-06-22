package com.upimesh.bankgateway.model.request;

import com.upimesh.bankgateway.model.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkBankAccountRequest {

    @NotBlank(message = "User UPI ID is required")
    private String userUpiId;

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^[0-9]{9,18}$", message = "Account number must be between 9 and 18 digits")
    private String accountNumber;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format")
    private String ifscCode;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @Builder.Default
    private boolean setPrimary = false;
}
