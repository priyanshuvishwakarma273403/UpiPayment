package com.upimesh.subscription.model.entity;

import com.upimesh.subscription.model.enums.PaymentAttemptStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_payment_attempts", indexes = {
        @Index(name = "idx_attempt_id", columnList = "attemptId", unique = true),
        @Index(name = "idx_attempt_sub_id", columnList = "subscriptionId"),
        @Index(name = "idx_attempt_txn_id", columnList = "transactionId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String attemptId;

    @Column(nullable = false, length = 40)
    private String subscriptionId;

    @Column(length = 50)
    private String transactionId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentAttemptStatus status;

    @Column(nullable = false)
    private int attemptNumber;

    @Column(length = 200)
    private String failureReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime attemptedAt;

    private LocalDateTime completedAt;
}
