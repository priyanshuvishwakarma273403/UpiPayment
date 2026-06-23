package com.upimesh.subscription.model.request;

import com.upimesh.subscription.model.enums.BillingCycle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "User UPI ID is required")
    private String userUpiId;

    @NotBlank(message = "Merchant UPI ID is required")
    private String merchantUpiId;

    @NotBlank(message = "Merchant name is required")
    private String merchantName;

    @NotBlank(message = "Plan name is required")
    private String planName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum subscription amount is 1.00")
    private BigDecimal amount;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;
}
