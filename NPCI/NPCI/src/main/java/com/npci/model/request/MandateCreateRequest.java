package com.npci.model.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * Request for POST /npci/mandate-create
 * Creates a recurring auto-debit mandate (like UPI AutoPay)
 */
@Data
public class MandateCreateRequest {

    @NotBlank(message = "User UPI ID is required")
    private String userUpiId;      // Who gets debited

    @NotBlank(message = "Merchant UPI ID is required")
    private String merchantUpiId;    // Who receives money

    @NotBlank(message = "Merchant name is required")
    private String merchantName;

    @NotNull(message = "Max amount is required")
    @DecimalMin(value = "1.00")
    @DecimalMax(value = "1500000.00", message = "Max mandate amount is ₹15 lakh")
    private BigDecimal maxAmount;       // User consents to this max limit

    @NotBlank(message = "Frequency is required")
    private String frequency;           // "MONTHLY", "WEEKLY", "DAILY", "AS_PRESENTED"

    private Integer executionDay;       // 1-28 for monthly (avoid 29,30,31 edge cases)

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in future")
    private LocalDate endDate;

    @NotBlank(message = "Purpose is required")
    private String purpose;             // "SUBSCRIPTION", "LOAN_EMI", "INSURANCE", "UTILITY"
}
