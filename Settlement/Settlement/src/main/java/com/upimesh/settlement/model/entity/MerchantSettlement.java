package com.upimesh.settlement.model.entity;

import com.upimesh.settlement.model.enums.SettlementMode;
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
@Table(name = "merchant_settlements", indexes = {
        @Index(name = "idx_settlement_id", columnList = "settlementId", unique = true),
        @Index(name = "idx_ms_batch_id", columnList = "batchId"),
        @Index(name = "idx_merchant_upi", columnList = "merchantUpiId"),
        @Index(name = "idx_ms_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String settlementId;

    @Column(nullable = false, length = 40)
    private String batchId;

    @Column(nullable = false, length = 100)
    private String merchantUpiId;

    @Column(nullable = false, length = 512)
    private String merchantBankAccount;

    @Column(nullable = false, length = 11)
    private String merchantIfsc;

    @Column(nullable = false, length = 100)
    private String merchantBankName;

    private int transactionCount;

    @Column(precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal platformFee;

    @Column(precision = 15, scale = 2)
    private BigDecimal gstOnFee;

    @Column(precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    @Column(length = 50)
    private String bankReferenceNumber;

    @Column(length = 200)
    private String failureReason;

    @Column(nullable = false)
    private LocalDate settlementDate;

    @Builder.Default
    private int retryCount = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;
}
