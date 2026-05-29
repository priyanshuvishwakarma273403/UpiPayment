package com.paymentService.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Payment Request DTO
 * Client yeh bhejta hai payment initiate karne ke liye
 */
@Data
public class PaymentRequest {

    @NotBlank(message = "Sender UPI ID required")
    private String senderUpiId;

    @NotBlank
    private String receiverUpiId;

    // receiverId UPI se resolve hoga, but convenience ke liye optional
    private Long receiverId;

    @NotNull(message = "Amount required")
    @DecimalMin(value = "1.00", message = "Minimum payment amount is ₹1")
    private BigDecimal amount;

    private String description;  // Payment ka reason

    // Optional: Custom idempotency key (client generate kar sakta hai)
    private String clientIdempotencyKey;

}
