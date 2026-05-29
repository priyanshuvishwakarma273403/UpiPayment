package com.paymentService.kafka;

import com.paymentService.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Event - Kafka messages mein yeh object serialize hoga
 * JSON format mein Kafka topics par jayega
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private String paymentId;
    private Long senderId;
    private Long receiverId;
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private String paymentStatus;
    private String paymentMode;
    private String description;
    private Double fraudScore;
    private String fraudStatus;
    private LocalDateTime timestamp;

    // Factory method from Payment entity
    public static PaymentEvent fromPayment(Payment payment) {
        return PaymentEvent.builder()
                .paymentId(payment.getPaymentId())
                .senderId(payment.getSenderId())
                .receiverId(payment.getReceiverId())
                .senderUpiId(payment.getSenderUpiId())
                .receiverUpiId(payment.getReceiverUpiId())
                .amount(payment.getAmount())
                .paymentStatus(payment.getPaymentStatus().name())
                .paymentMode(payment.getPaymentMode().name())
                .description(payment.getDescription())
                .fraudScore(payment.getFraudScore())
                .fraudStatus(payment.getFraudStatus().name())
                .timestamp(LocalDateTime.now())
                .build();
    }

}
