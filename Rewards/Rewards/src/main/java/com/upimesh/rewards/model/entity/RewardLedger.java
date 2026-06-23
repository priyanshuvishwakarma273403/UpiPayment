package com.upimesh.rewards.model.entity;

import com.upimesh.rewards.model.enums.RewardStatus;
import com.upimesh.rewards.model.enums.RewardType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reward_ledger", indexes = {
        @Index(name = "idx_ledger_id", columnList = "ledgerId", unique = true),
        @Index(name = "idx_ledger_user_id", columnList = "userId"),
        @Index(name = "idx_ledger_user_upi", columnList = "userUpiId"),
        @Index(name = "idx_ledger_txn_id", columnList = "transactionId"),
        @Index(name = "idx_ledger_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String ledgerId;

    @Column(nullable = false, length = 100)
    private String userId;

    @Column(nullable = false, length = 100)
    private String userUpiId;

    @Column(length = 60)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RewardType rewardType;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal cashbackAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RewardStatus status;

    @Column(length = 200)
    private String description;

    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
