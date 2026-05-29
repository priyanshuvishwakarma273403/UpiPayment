package com.transaction_service.entity.mysql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ================================================================
 * Transaction Entity - MySQL Table: transactions
 * ================================================================
 * Yeh payment ka confirmed ledger record hai.
 * Payment complete hone par Kafka event aata hai aur
 * yahan entry hoti hai.
 *
 * Double Entry Principle:
 * Har successful payment ke liye:
 *   1. DEBIT entry (sender)
 *   2. CREDIT entry (receiver)
 * Dono same paymentId se linked hain.
 * ================================================================
 */

@Entity
@Table(name = "transactions",
indexes = {
        @Index(name = "idx_txn_payment_id", columnList = "payment_id"),
        @Index(name = "idx_txn_user_id",    columnList = "user_id"),
        @Index(name = "idx_txn_merchant_id", columnList = "merchant_id"),
        @Index(name = "idx_txn_created_at",  columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, length = 60)
    private String paymentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "merchant_id")
    private Long merchantId;    // Null if peer-to-peer

    @Column(name = "counter_party_id")
    private Long counterPartyId; // Sender ya receiver dusra

    @Column(name = "counter_party_upi_id", length = 60)
    private String counterPartyUpiId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10)
    private TransactionType transactionType; // DEBIT / CREDIT

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 20)
    private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.SUCCESS;

    @Column(length = 500)
    private String description;

    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter;  // Wallet balance after this transaction

    @Column(name = "reference_number", unique = true, length = 80)
    private String referenceNumber;  // UPI reference number

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        DEBIT,  // Paisa gaya
        CREDIT  // Paisa aaya
    }

    public enum PaymentMode {
        UPI, QR, OFFLINE, BANK_TRANSFER
    }

    public enum TransactionStatus {
        SUCCESS, FAILED, REVERSED, PENDING
    }

}
