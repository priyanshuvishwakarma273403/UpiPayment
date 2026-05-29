package com.walletService.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Debit / Credit Request DTO
 * Payment service OpenFeign ke through yeh call karega
 */
@Data
public class DebitCreditRequest {

    @NotNull(message = "userId required")
    private Long userId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotBlank(message = "paymentId required")
    private String paymentId;

    private String description;
}
