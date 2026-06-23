package com.upimesh.dispute.model.entity;

import com.upimesh.dispute.model.converter.StringListConverter;
import com.upimesh.dispute.model.enums.DisputeReason;
import com.upimesh.dispute.model.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "disputes", indexes = {
        @Index(name = "idx_dispute_id", columnList = "disputeId", unique = true),
        @Index(name = "idx_dispute_txn_id", columnList = "transactionId"),
        @Index(name = "idx_dispute_user_id", columnList = "userId"),
        @Index(name = "idx_dispute_user_upi", columnList = "userUpiId"),
        @Index(name = "idx_dispute_merchant_upi", columnList = "merchantUpiId"),
        @Index(name = "idx_dispute_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String disputeId;

    @Column(nullable = false, length = 100)
    private String userId;

    @Column(nullable = false, length = 60)
    private String transactionId;

    @Column(nullable = false, length = 100)
    private String userUpiId;

    @Column(nullable = false, length = 100)
    private String merchantUpiId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DisputeReason reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DisputeStatus status;

    @Column(length = 500)
    private String userDescription;

    @Column(length = 500)
    private String merchantResponse;

    @Convert(converter = StringListConverter.class)
    @Lob
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private List<String> evidenceUrls = new ArrayList<>();

    @Column(length = 20)
    private String resolvedInFavorOf; // "USER" or "MERCHANT"

    @Column(length = 500)
    private String resolutionNotes;

    @Column(nullable = false)
    private LocalDateTime raisedAt;

    @Column(nullable = false)
    private LocalDateTime merchantResponseDeadline;

    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
