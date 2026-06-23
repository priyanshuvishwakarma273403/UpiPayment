package com.upimesh.referral.model.entity;

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
@Table(name = "referral_codes", indexes = {
        @Index(name = "idx_ref_code_id", columnList = "codeId", unique = true),
        @Index(name = "idx_ref_user_id", columnList = "userId", unique = true),
        @Index(name = "idx_ref_code_str", columnList = "code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codeId;

    @Column(nullable = false, unique = true, length = 100)
    private String userId;

    @Column(nullable = false, length = 100)
    private String userUpiId;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false)
    @Builder.Default
    private int totalReferrals = 0;

    @Column(nullable = false)
    @Builder.Default
    private int qualifiedReferrals = 0;

    @Column(nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalEarned = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
