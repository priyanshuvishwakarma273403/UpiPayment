package com.npci.model.entity;

import com.npci.model.enums.TransactionStatus;
import com.npci.model.enums.TransactionType;
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
 * UpiTransaction — Core entity that tracks every payment request
 * sent to NPCI and its lifecycle.
 *
 * One row = One payment attempt
 * NPCI gives back their txnId which we store as npciTransactionId
 */

@Entity
@Table(name = "upi_transactions", indexes = {
        @Index(name = "idx_txn_id" , columnList = "transactionId"),
        @Index(name = "idx_npci_txn_id", columnList = "npciTransactionId"),
        @Index(name = "idx_sender_upi", columnList = "senderUpiId"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpiTransaction {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    // Our internal unique ID (UUID format)
    @Column(nullable = false, unique = true, length = 50)
    private String transactionId;

    // NPCI's transaction reference — comes back in NPCI response
    @Column(length = 50)
    private String npciTransactionId;

    // RRN = Retrieval Reference Number (from bank, used for disputes)
    @Column(length = 50)
    private String rrn;

    // UPI IDs
    @Column(nullable = false, length = 100)
    private String senderUpiId;     // e.g. 9876543210@upimesh

    @Column(nullable = false, length = 100)
    private String receiverUpiId;    // e.g. merchant@hdfc

    // Amount in INR (BigDecimal for precision — never use float for money!)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // Optional — payment note visible to both parties
    @Column(length = 200)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    // NPCI response codes
    @Column(length = 10)
    private String npciResponseCode;    // "00" = success, "Z9" = insufficient funds, etc.

    @Column(length = 200)
    private String npciResponseMessage;

    // Retry tracking
    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRetried = false;

    // Refund link — if this transaction was refunded
    @Column(length = 50)
    private String refundTransactionId;

    // Original txn ID — if this transaction IS a refund
    @Column(length = 50)
    private String originalTransactionId;

    // Device info (for fraud detection)
    @Column(length = 100)
    private String deviceId;

    @Column(length = 50)
    private String ipAddress;

    // Timestamps
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // When NPCI responded
    private LocalDateTime npciResponseAt;

    // When transaction was completed (success or fail)
    private LocalDateTime completedAt;

}
