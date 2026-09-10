package com.fraudService.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Fraud Check Request DTO
 * payment-service yeh bhejega (ya Kafka event se parse hoga)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckRequest {

    @NotBlank(message = "payment required")
    private String paymentId;

    @NotNull(message = "senderId required")
    private Long senderId;

    private Long receiverId;

    private String senderUpiId;

    @NotBlank(message = "receiverUpiId required")
    private String receiverUpiId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private String paymentMode; // UPI / QR / OFFLINE

    private String deviceId;    // Optional: device fingerprint

    private String ipAddress;   // Optional: for geo-analysis


}
