package com.syncService.Sync.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ================================================================
 * SyncRecord Entity - MySQL Table: sync_records
 * ================================================================
 * Har offline payment ka ek sync record hoga.
 * Yeh track karta hai:
 * - Payment sync hua ya nahi
 * - Kitni baar retry hua
 * - Kyun fail hua (last failure reason)
 * - Next retry kab hogi
 * ================================================================
 */
@Entity
@Table(name = "sync_records",
        indexes = {
                @Index(name = "idx_sync_payment_id", columnList = "payment_id", unique = true),
                @Index(name = "idx_sync_sender_id",  columnList = "sender_id"),
                @Index(name = "idx_sync_status",     columnList = "status")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, unique = true, length = 60)
    private String paymentId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    // Offline payment ka complete JSON (signature included)
    @Column(name = "payment_json", columnDefinition = "TEXT", nullable = false)
    private String paymentJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private SyncStatus status = SyncStatus.PENDING;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "last_failure_reason", length = 500)
    private String lastFailureReason;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum SyncStatus {
        PENDING,             // Sync hona baaki hai
        SYNCED,              // Successfully synced
        FAILED,              // Failed but will retry
        PERMANENTLY_FAILED   // Max retries exhausted
    }


}
