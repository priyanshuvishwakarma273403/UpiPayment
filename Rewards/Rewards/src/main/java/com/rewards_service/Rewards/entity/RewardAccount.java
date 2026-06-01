package com.rewards_service.Rewards.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * ================================================================
 * RewardAccount - MySQL Table: reward_accounts
 * ================================================================
 * Har user ka reward/points balance.
 * Dual currency: Points + Cashback (INR)
 *
 * Points earning:
 *   UPI Payment: 1 point per ₹10
 *   First payment: 100 bonus points
 *   Referral: 500 points per successful referral
 *
 * Redemption:
 *   100 points = ₹1 cashback
 *   Minimum redemption: 500 points
 * ================================================================
 */
@Entity
@Table(name = "reward_accounts",
indexes = {
        @Index(name = "idx_reward_user_id", columnList = "user_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardAccount {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id" , nullable = false, unique = true)
    private Long userId;

    @Column(name = "total_points")
    @Builder.Default
    private Long totalPoints = 0L;

    @Column(name = "redeemed_points")
    @Builder.Default
    private Long redeemedPoints = 0L;

    @Column(name = "expired_points")
    private Long expiredPoints;

    // Available = total - redeemed - expired
    @Column(name = "available_points")
    private Long availablePoints = 0L;

    // Cashback balance (INR) - direct credit
    @Column(name = "cashback_balance", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cashbackBalance = BigDecimal.ZERO;

    @Column(name = "total_cashback_earned", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalCashbackEarned = BigDecimal.ZERO;

    @Column(name = "tier", length = 15)
    @Builder.Default
    private String tier = "BRONZE";   // BRONZE, SILVER, GOLD, PLATINUM

    @Column(name = "tier_expiry")
    private LocalDateTime tierExpiry;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;






}
