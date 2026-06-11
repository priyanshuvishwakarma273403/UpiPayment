package com.npci.model.entity;

import com.npci.model.enums.MandateStatus;
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


/**
 * UpiMandate — Represents a recurring auto-debit setup.
 * E.g.: Netflix ₹499 every month from user's UPI handle.
 *
 * Once ACTIVE, scheduler will auto-debit on executionDay each month.
 */
@Entity
@Table(name = "upi_mandated", indexes = {
        @Index(name = "idx_mandate_id", columnList = "mandateId"),
        @Index(name = "idx_user_upi", columnList = "userUpiId"),
        @Index(name = "idx_mandate_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpiMandate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String mandateId;         // Our internal mandate ID

    @Column(length = 50)
    private String npciMandateId;     // NPCI's mandate reference

    // Who is being debited
    @Column(nullable = false, length = 100)
    private String userUpiId;

    // Who receives the money (merchant)
    @Column(nullable = false, length = 100)
    private String merchantUpiId;

    @Column(nullable = false, length = 100)
    private String merchantName;

    // Max amount per debit (user has to consent to this)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal maxAmount;

    // Actual debit amount (can be <= maxAmount)
    @Column(precision = 12, scale = 2)
    private BigDecimal currentAmount;

    // Recurrence — "MONTHLY", "WEEKLY", "DAILY", "AS_PRESENTED"
    @Column(nullable = false, length = 20)
    private String frequency;

    // Day of month to execute (1-31), or day of week (1-7)
    private Integer executionDay;

    // Mandate validity window
    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // Last successful execution date
    private LocalDate lastExecutedDate;

    // Next scheduled execution
    private LocalDate nextExecutionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MandateStatus status;

    // Purpose — "SUBSCRIPTION", "LOAN_EMI", "INSURANCE", "UTILITY"
    @Column(length = 30)
    private String purpose;

    // Retry count if last execution failed
    @Builder.Default
    private Integer failedAttempts = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
