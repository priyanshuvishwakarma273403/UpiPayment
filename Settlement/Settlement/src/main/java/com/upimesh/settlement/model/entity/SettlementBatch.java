package com.upimesh.settlement.model.entity;

import com.upimesh.settlement.model.enums.SettlementStatus;
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
@Table(name = "settlement_batches", indexes = {
        @Index(name = "idx_batch_id", columnList = "batchId", unique = true),
        @Index(name = "idx_settlement_date", columnList = "settlementDate"),
        @Index(name = "idx_batch_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String batchId;

    @Column(nullable = false)
    private LocalDate settlementDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private int totalMerchants;

    private int totalTransactions;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal processedAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal failedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
