package com.upimesh.reconciliation.model.entity;

import com.upimesh.reconciliation.model.enums.ReconciliationStatus;
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
@Table(name = "reconciliation_discrepancies", indexes = {
        @Index(name = "idx_disc_report_id", columnList = "reportId"),
        @Index(name = "idx_disc_txn_id", columnList = "transactionId"),
        @Index(name = "idx_disc_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationDiscrepancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String discrepancyId;

    @Column(nullable = false, length = 40)
    private String reportId;

    @Column(length = 50)
    private String transactionId;

    @Column(length = 50)
    private String bankReferenceNumber;

    @Column(precision = 15, scale = 2)
    private BigDecimal ourAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal bankAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReconciliationStatus status;

    @Column(length = 500)
    private String description;

    @Column(length = 300)
    private String resolution;

    private LocalDateTime resolvedAt;

    @Column(length = 100)
    private String resolvedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
