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
    private String eventId;
    private String eventType;
    private String eventVersion;
    private LocalDateTime occurredAt;
    private String correlationId;
    private String currency;

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
        return fromPayment(payment, null, payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : "TRANSACTION_EVENT");
    }

    public static PaymentEvent fromPayment(Payment payment, String correlationId, String eventType) {
        String evtId = "EVT-" + java.util.UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        return PaymentEvent.builder()
                .eventId(evtId)
                .eventType(eventType != null ? eventType : (payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : "TRANSACTION_EVENT"))
                .eventVersion("v1")
                .occurredAt(now)
                .correlationId(correlationId != null ? correlationId : evtId)
                .currency("INR")
                .paymentId(payment.getPaymentId())
                .senderId(payment.getSenderId())
                .receiverId(payment.getReceiverId())
                .senderUpiId(payment.getSenderUpiId())
                .receiverUpiId(payment.getReceiverUpiId())
                .amount(payment.getAmount())
                .paymentStatus(payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : "INITIATED")
                .paymentMode(payment.getPaymentMode() != null ? payment.getPaymentMode().name() : "UPI")
                .description(payment.getDescription())
                .fraudScore(payment.getFraudScore())
                .fraudStatus(payment.getFraudStatus() != null ? payment.getFraudStatus().name() : "SAFE")
                .timestamp(now)
                .build();
    }
}
