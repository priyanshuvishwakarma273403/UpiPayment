package com.paymentService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ================================================================
 * Payment Entity - MySQL Table: payments
 * ================================================================
 * Yeh har payment transaction ka record hai.
 *
 * Payment Modes:
 * - UPI: Normal online UPI payment
 * - QR: Merchant QR code scan karke payment
 * - OFFLINE: Internet nahi hai tab queued payment
 *
 * Payment Status Flow:
 * INITIATED -> (fraud check) -> PENDING_SYNC -> SUCCESS
 *                                            -> FAILED
 *
 * Security:
 * - signature: RSA private key se payment payload ka hash
 * - Verify karte waqt public key se verify hoga
 * ================================================================
 */

@Entity
@Table(name = "payments",
indexes = {
        @Index(name = "idx_payments_sender", columnList = "sender_id"),
        @Index(name = "idx_payments_receiver", columnList = "receiver_id"),
        @Index(name = "idx_payments_status", columnList = "payment_status"),
        @Index(name = "idx_payments_timestamp", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @Column(name = "payment_id", length = 50)
    private String paymentId; // UUID format : PAY-UUID

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "sender_upi_id", length = 50)
    private String senderUpiId;

    @Column(name = "receiver_upi_id", length = 50)
    private String receiverUpiId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.INITIATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 20)
    private PaymentMode paymentMode;

    // AES encrypted payment description
    @Column(name = "description", length = 500)
    private String description;

    // RSA signature of: paymentId + senderId + receiverId + amount + timestamp
    @Column(name = "signature", columnDefinition = "TEXT")
    private String signature;

    // Offline payments ke liye - queue mein tha tab ka timestamp
    @Column(name = "queued_at")
    private LocalDateTime queuedAt;

    // Idempotency key - same payment dobara process na ho
    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "fraud_score")
    private Double fraudScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "fraud_status", length = 20)
    @Builder.Default
    private FraudStatus fraudStatus = FraudStatus.SAFE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---- Enums ----
    public enum PaymentStatus {
        INITIATED,      // Payment abhi start hui
        PENDING_SYNC,   // Offline queue mein hai
        PROCESSING,     // Fraud check + wallet debit ho raha hai
        SUCCESS,        // Payment complete
        FAILED,         // Payment fail
        REVERSED        // Reversal ho gayi (dispute case)
    }

    public enum PaymentMode {
        UPI,            // Normal UPI
        QR,             // QR code scan
        OFFLINE         // Offline queued
    }

    public enum FraudStatus {
        SAFE,
        REVIEW,
        BLOCKED
    }


}
