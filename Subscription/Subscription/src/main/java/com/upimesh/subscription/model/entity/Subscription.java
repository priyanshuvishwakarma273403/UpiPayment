package com.upimesh.subscription.model.entity;

import com.upimesh.subscription.model.enums.BillingCycle;
import com.upimesh.subscription.model.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_sub_id", columnList = "subscriptionId", unique = true),
        @Index(name = "idx_sub_user_id", columnList = "userId"),
        @Index(name = "idx_sub_user_upi_id", columnList = "userUpiId"),
        @Index(name = "idx_sub_next_billing", columnList = "nextBillingDate"),
        @Index(name = "idx_sub_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String subscriptionId;

    @Column(nullable = false, length = 100)
    private String userId;

    @Column(nullable = false, length = 100)
    private String userUpiId;

    @Column(nullable = false, length = 100)
    private String merchantUpiId;

    @Column(nullable = false, length = 100)
    private String merchantName;

    @Column(nullable = false, length = 100)
    private String planName;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BillingCycle billingCycle;

    @Column(length = 50)
    private String mandateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionStatus status;

    private LocalDateTime trialEndsAt;

    private LocalDateTime currentPeriodStart;

    private LocalDateTime currentPeriodEnd;

    private LocalDate nextBillingDate;

    private LocalDateTime cancelledAt;

    @Column(length = 200)
    private String cancelReason;

    @Builder.Default
    @Column(nullable = false)
    private int failedAttempts = 0;

    @Builder.Default
    @Column(nullable = false)
    private int maxRetries = 3;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
