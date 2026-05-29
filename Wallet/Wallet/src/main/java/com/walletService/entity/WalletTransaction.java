package com.walletService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ================================================================
 * WalletTransaction Entity - MySQL Table: wallet_transactions
 * ================================================================
 * Double-entry bookkeeping ke liye har debit/credit ka record.
 * Yeh audit trail hai - kab kitna paisa aaya/gaya sab track hoga.
 *
 * Note: Yeh payment ki transaction nahi hai.
 * Yeh wallet ke andar movements hain.
 * Payment transactions -> transaction-service mein hain.
 * ================================================================
 */
@Entity
@Table(name = "wallet_transactions",
indexes = {
        @Index(name = "idx_wt_wallet_id", columnList = "wallet_id"),
        @Index(name = "idx_wt_user_id", columnList = "user_id"),
        @Index(name = "idx_wt_time", columnList = "transaction_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10)
    private TransactionType transactionType;

    // Transaction ke baad wallet ka balance (snapshot)
    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 300)
    private String description;

    @Column(name = "reference_id", length = 100)
    private String referenceId; // paymentId ya bank reference

    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;

    public enum TransactionType {
        CREDIT,  // Paisa aaya
        DEBIT    // Paisa gaya
    }

}
