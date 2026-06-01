package com.rewards_service.Rewards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * RewardTransaction - MySQL Table: reward_transactions
 * Every points earn/redeem/expire ka record
 */
@Entity
@Table(name = "reward_transactions",
indexes = {
        @Index(name = "idx_rtxn_user_id",    columnList = "user_id"),
        @Index(name = "idx_rtxn_payment_id", columnList = "reference_id"),
        @Index(name = "idx_rtxn_type",       columnList = "transaction_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reference_id", length = 60)
    private String referenceId;     // paymentId / referralCode / offerId

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private RewardTxnType transactionType;

    @Column(name = "points_change")
    private Long pointsChange;      // +ve = earned, -ve = redeemed/expired

    @Column(name = "cashback_amount", precision = 10, scale = 2)
    private BigDecimal cashbackAmount;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "balance_after_points")
    private Long balanceAfterPoints;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum RewardTxnType {
        EARNED_PAYMENT,      // Payment karne par points
        EARNED_REFERRAL,     // Referral bonus
        EARNED_SIGNUP,       // Welcome bonus
        EARNED_OFFER,        // Offer/campaign se
        REDEEMED_CASHBACK,   // Points -> wallet credit
        EXPIRED,             // Points expire
        ADJUSTED             // Manual admin adjustment
    }
}
