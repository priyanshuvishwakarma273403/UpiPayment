package com.upimesh.payroll.model.entity;

import com.upimesh.payroll.model.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_batches", indexes = {
        @Index(name = "idx_payroll_batch_id", columnList = "batchId", unique = true),
        @Index(name = "idx_payroll_company_id", columnList = "companyId"),
        @Index(name = "idx_payroll_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String batchId;

    @Column(nullable = false, length = 100)
    private String companyId;

    @Column(nullable = false, length = 100)
    private String companyUpiId;

    @Column(nullable = false, length = 7) // "YYYY-MM"
    private String month;

    @Column(nullable = false)
    private int employeeCount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalGrossAmount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalDeductions;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalNetAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayrollStatus status;

    @Column(nullable = false)
    @Builder.Default
    private int processedCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int failedCount = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;
}
