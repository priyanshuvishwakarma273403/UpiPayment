package com.walletService.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddMoneyRequest {

    private Long userId;

    @NotNull
    @DecimalMin(value = "1.00", message = "Minimum  ₹1 required")
    private BigDecimal amount;

    private String bankReference; // Bank transaction reference

}
