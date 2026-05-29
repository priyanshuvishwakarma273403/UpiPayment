package com.paymentService.dto.response;

import com.paymentService.entity.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Response DTO
 * Client ko payment result mein yeh milega
 */
@Data
@Builder
public class PaymentResponse {

    private String paymentId;
    private Long senderId;
    private Long receiverId;
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private String paymentStatus;
    private String paymentMode;
    private String description;
    private String fraudStatus;
    private Double fraudScore;
    private LocalDateTime createdAt;
    private String message;  // Human readable message

    /** Entity se Response DTO banao */
    public static PaymentResponse fromPayment(Payment payment) {
        String msg = switch (payment.getPaymentStatus()){
            case INITIATED   -> "Payment initiated. Processing...";
            case PENDING_SYNC -> "Payment queued for offline sync.";
            case PROCESSING  -> "Payment is being processed.";
            case SUCCESS     -> "Payment successful!";
            case FAILED      -> "Payment failed. " + (payment.getFailureReason() != null ? payment.getFailureReason() : "");
            case REVERSED    -> "Payment has been reversed.";
        };

        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .senderId(payment.getSenderId())
                .receiverId(payment.getReceiverId())
                .senderUpiId(payment.getSenderUpiId())
                .receiverUpiId(payment.getReceiverUpiId())
                .amount(payment.getAmount())
                .paymentStatus(payment.getPaymentStatus().name())
                .paymentMode(payment.getPaymentMode().name())
                .description(payment.getDescription())
                .fraudStatus(payment.getFraudStatus() != null ? payment.getFraudStatus().name() : "SAFE")
                .fraudScore(payment.getFraudScore())
                .createdAt(payment.getCreatedAt())
                .message(msg)
                .build();
    }

}
