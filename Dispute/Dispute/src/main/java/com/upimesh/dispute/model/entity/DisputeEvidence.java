package com.upimesh.dispute.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dispute_evidences", indexes = {
        @Index(name = "idx_evidence_id", columnList = "evidenceId", unique = true),
        @Index(name = "idx_evidence_dispute_id", columnList = "disputeId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String evidenceId;

    @Column(nullable = false, length = 40)
    private String disputeId;

    @Column(nullable = false, length = 20)
    private String submittedBy; // "USER" or "MERCHANT"

    @Column(nullable = false, length = 30)
    private String evidenceType; // "SCREENSHOT", "RECEIPT", "STATEMENT"

    @Column(length = 200)
    private String description;

    @Column(nullable = false, length = 200)
    private String fileUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;
}
