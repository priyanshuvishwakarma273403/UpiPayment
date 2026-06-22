package com.upimesh.reconciliation.model.entity;

import com.upimesh.reconciliation.model.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reconciliation_reports", indexes = {
        @Index(name = "idx_report_id", columnList = "reportId", unique = true),
        @Index(name = "idx_reconcile_date", columnList = "reconciliationDate")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String reportId;

    @Column(nullable = false)
    private LocalDate reconciliationDate;

    private int totalSystemTransactions;

    private int totalBankTransactions;

    private int matchedCount;

    private int mismatchCount;

    private int missingInBankCount;

    private int missingInSystemCount;

    private int duplicateCount;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalSystemAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalBankAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal discrepancyAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
