package com.upimesh.loan.model.entity;

import com.upimesh.loan.model.enums.LoanStatus;
import com.upimesh.loan.model.enums.LoanType;
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
@Table(name = "loan_applications", indexes = {
        @Index(name = "idx_loan_app_id", columnList = "applicationId", unique = true),
        @Index(name = "idx_loan_user_id", columnList = "userId"),
        @Index(name = "idx_loan_user_upi", columnList = "userUpiId"),
        @Index(name = "idx_loan_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String applicationId;

    @Column(nullable = false, length = 100)
    private String userId;

    @Column(nullable = false, length = 100)
    private String userUpiId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoanType loanType;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal requestedAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal approvedAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate; // Annual interest rate in %

    @Column(precision = 18, scale = 2)
    private BigDecimal processingFee;

    @Column(nullable = false)
    private int tenureMonths;

    @Column(precision = 18, scale = 2)
    private BigDecimal emiAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoanStatus status;

    private int creditScore;

    private LocalDateTime disbursedAt;

    private LocalDateTime closedAt;

    @Column(length = 255)
    private String rejectionReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
