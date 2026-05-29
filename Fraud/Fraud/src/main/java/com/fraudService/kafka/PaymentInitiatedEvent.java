package com.fraudService.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Event received from payment_initiated topic */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiatedEvent {

    private String paymentId;
    private Long senderId;
    private Long receiverId;
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private String paymentStatus;
    private String paymentMode;
    private LocalDateTime timestamp;

}
