package com.upimesh.loan.model.entity;

import com.upimesh.loan.model.enums.RepaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_repayments", indexes = {
        @Index(name = "idx_repayment_id", columnList = "repaymentId", unique = true),
        @Index(name = "idx_repayment_app_id", columnList = "applicationId"),
        @Index(name = "idx_repayment_status", columnList = "status"),
        @Index(name = "idx_repayment_due_date", columnList = "dueDate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String repaymentId;

    @Column(nullable = false, length = 40)
    private String applicationId;

    @Column(nullable = false)
    private int emiNumber;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal emiAmount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal principalComponent;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal interestComponent;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepaymentStatus status;

    @Column(length = 60)
    private String transactionId;

    @Builder.Default
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;
}
