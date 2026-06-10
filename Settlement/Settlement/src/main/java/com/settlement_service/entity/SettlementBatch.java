package com.settlement_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * ================================================================
 * SettlementBatch - MySQL Table: settlement_batches
 * ================================================================
 * Har din ka merchant settlement batch.
 * T+1 settlement: Aaj ke transactions kal settle honge.
 *
 * Flow:
 * 1. EOD (End of Day): Batch create hoti hai
 * 2. Transactions aggregate hote hain
 * 3. MDR (Merchant Discount Rate) cut hoti hai
 * 4. Net amount bank transfer hota hai (NEFT/RTGS/IMPS)
 * 5. Settlement report generate hoti hai
 * ================================================================
 */

@Entity
@Table(name = "settlement_batches",
indexes = {
        @Index(name = "idx_settle_merchant", columnList = "merchant_id"),
        @Index(name = "idx_settle_date",     columnList = "settlement_date"),
        @Index(name = "idx_settle_status",   columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id",   unique = true, nullable = false, length = 40)
    private String batchId;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "transaction_count")
    private Integer transactionCount;

    @Column(name = "gross_amount", precision = 15, scale = 2)
    private BigDecimal grossAmount;

    // MDR = Merchant Discount Rate (e.g. 0.9% for UPI)
    @Column(name = "mdr_rate", precision = 5, scale = 4)
    private BigDecimal mdrRate;

    @Column(name = "mdr_amount", precision = 15, scale = 2)
    private BigDecimal mdrAmount;

    // GST on MDR (18%)
    @Column(name = "gst_amount", precision = 15, scale = 2)
    private BigDecimal gstAmount;

    @Column(name = "net_amount", precision = 15, scale = 2)
    private BigDecimal netAmount;       // grossAmount - mdrAmount - gstAmount

    // Bank transfer details
    @Column(name = "bank_account_number", length = 20)
    private String bankAccountNumber;

    @Column(name = "bank_ifsc", length = 15)
    private String bankIfsc;

    @Column(name = "bank_reference", length = 50)
    private String bankReference;       // NEFT/RTGS UTR number

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "failure_reason", length = 300)
    private String failureReason;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum SettlementStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, REVERSED
    }

}
