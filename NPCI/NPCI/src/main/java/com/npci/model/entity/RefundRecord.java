package com.npci.model.entity;

import com.npci.model.enums.RefundStatus;
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
 * RefundRecord — Tracks refund requests for failed/disputed transactions.
 * A refund is essentially a reverse payment via NPCI.
 */

@Entity
@Table(name = "refund_records", indexes = {
        @Index(name = "idx_refund_id", columnList = "refundId"),
        @Index(name = "idx_original_txn", columnList = "originalTransactionId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String refundId;

    // The original payment being refunded
    @Column(nullable = false, length = 50)
    private String originalTransactionId;

    // NPCI's refund transaction ID
    @Column(length = 50)
    private String npciRefundTxnId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    // Who gets the refund (the original sender)
    @Column(nullable = false, length = 100)
    private String refundToUpiId;

    // Reason: "DUPLICATE", "FRAUD", "MERCHANT_REQUEST", "SYSTEM_ERROR"
    @Column(nullable = false, length = 50)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status;

    @Column(length = 10)
    private String npciResponseCode;

    @Column(length = 200)
    private String npciResponseMessage;

    // Refund processing can take T+1 to T+5 days
    private LocalDateTime expectedCreditBy;

    @Builder.Default
    private Integer retryCount = 0;

    // Who raised the refund: "SYSTEM", "MERCHANT", "USER", "ADMIN"
    @Column(length = 20)
    private String initiatedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

}
