package com.upimesh.rewards.model.entity;

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
@Table(name = "user_reward_accounts", indexes = {
        @Index(name = "idx_user_reward_id", columnList = "userId", unique = true),
        @Index(name = "idx_user_reward_upi", columnList = "userUpiId", unique = true),
        @Index(name = "idx_user_reward_ref", columnList = "referralCode", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRewardAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String userId;

    @Column(nullable = false, unique = true, length = 100)
    private String userUpiId;

    @Column(nullable = false)
    private int totalPointsEarned;

    @Column(nullable = false)
    private int totalPointsRedeemed;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalCashbackEarned;

    @Column(nullable = false)
    private int availablePoints;

    @Column(nullable = false, unique = true, length = 10)
    private String referralCode;

    @Column(length = 100)
    private String referredByUserId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
