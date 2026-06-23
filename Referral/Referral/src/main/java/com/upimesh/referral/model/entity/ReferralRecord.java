package com.upimesh.referral.model.entity;

import com.upimesh.referral.model.enums.ReferralRewardStatus;
import com.upimesh.referral.model.enums.ReferralStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "referral_records", indexes = {
        @Index(name = "idx_ref_rec_id", columnList = "referralId", unique = true),
        @Index(name = "idx_ref_rec_code", columnList = "referralCode"),
        @Index(name = "idx_ref_rec_referee", columnList = "refereeId"),
        @Index(name = "idx_ref_rec_status", columnList = "status"),
        @Index(name = "idx_ref_rec_ip", columnList = "ipAddress"),
        @Index(name = "idx_ref_rec_device", columnList = "deviceId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String referralId;

    @Column(nullable = false, length = 10)
    private String referralCode;

    @Column(nullable = false, length = 100)
    private String referrerId;

    @Column(nullable = false, length = 100)
    private String referrerUpiId;

    @Column(nullable = false, length = 100)
    private String refereeId;

    @Column(nullable = false, length = 100)
    private String refereeUpiId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReferralStatus status;

    @Column(length = 60)
    private String refereeFirstTransactionId;

    private LocalDateTime refereeFirstTransactionAt;

    @Column(nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal referrerRewardAmount = BigDecimal.valueOf(50.00); // Earn ₹50 program

    @Column(nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal refereeRewardAmount = BigDecimal.valueOf(25.00); // Referee gets ₹25

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReferralRewardStatus referrerRewardStatus = ReferralRewardStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReferralRewardStatus refereeRewardStatus = ReferralRewardStatus.PENDING;

    @Column(nullable = false, length = 100)
    private String deviceId;

    @Column(nullable = false, length = 45) // Length 45 accommodates IPv6
    private String ipAddress;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime qualifiedAt;

    private LocalDateTime rewardScheduledAt;
}
