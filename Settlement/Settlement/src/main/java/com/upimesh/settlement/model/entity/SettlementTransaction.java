package com.upimesh.settlement.model.entity;

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
@Table(name = "settlement_transactions", indexes = {
        @Index(name = "idx_st_settlement_id", columnList = "settlementId"),
        @Index(name = "idx_original_txn_id", columnList = "originalTransactionId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String settlementId;

    @Column(nullable = false, length = 50)
    private String originalTransactionId;

    @Column(nullable = false, length = 100)
    private String merchantUpiId;

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(precision = 15, scale = 2)
    private BigDecimal platformFee;

    @Column(precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime includedAt;
}
