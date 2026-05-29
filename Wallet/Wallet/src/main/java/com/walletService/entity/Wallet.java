package com.walletService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets",
indexes = {@Index(name = "idx_wallets_user_id", columnList = "user_is", unique = true)})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "upi_id", unique = true, length = 50)
    private String upiId;

    // Current balance
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    // Freeze amount - payment in progress mein freeze hoti hai
    // Yeh balance part of wallet hai but available nahi
    @Column(name = "frozen_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal frozenAmount = BigDecimal.ZERO;

    @Column(name = "daily_limit", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal dailyLimit = new BigDecimal("100000"); // 1 lakh default

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Optimistic Locking - concurrent updates handle karta hai
    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Available balance = balance - frozenAmount
     */
    public BigDecimal getAvailableBalance() {
        return balance.subtract(frozenAmount != null ? frozenAmount : BigDecimal.ZERO);
    }

    /**
     * Balance sufficient hai ya nahi check karo
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return getAvailableBalance().compareTo(amount) >= 0;
    }

}
