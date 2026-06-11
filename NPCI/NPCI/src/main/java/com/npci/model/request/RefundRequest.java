package com.npci.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request for POST /npci/refund
 */
@Data
public class RefundRequest {

    @NotBlank(message = "Original transaction ID is required")
    private String originalTransactionId;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "1.00", message = "Minimum refund is ₹1")
    private BigDecimal refundAmount;  // Can be partial refund

    @NotBlank(message = "Reason is required")
    private String reason;  // "DUPLICATE", "FRAUD", "MERCHANT_REQUEST", "SYSTEM_ERROR"

    @NotBlank(message = "Initiated by is required")
    private String initiatedBy;  // "SYSTEM", "MERCHANT", "USER", "ADMIN"

}
