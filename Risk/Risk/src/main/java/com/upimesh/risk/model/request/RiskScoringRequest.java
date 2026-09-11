package com.upimesh.risk.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskScoringRequest {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "User UPI ID is required")
    private String userUpiId;

    @NotBlank(message = "Receiver UPI ID is required")
    private String receiverUpiId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "IP Address is required")
    private String ipAddress;

    private String city;

    private String location;

    private String transactionType;

    private String merchantId;

    private String merchantCategory;
}
