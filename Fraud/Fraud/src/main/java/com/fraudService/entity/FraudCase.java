package com.fraudService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "fraud_cases")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCase {

    @Id
    private String caseId;

    @Indexed
    private String customerId;

    @Indexed
    private String transactionId;

    private String fraudType;

    // Severity: LOW, MEDIUM, HIGH, CRITICAL
    private String severity;

    // Status: OPEN, INVESTIGATING, ESCALATED, CONFIRMED_FRAUD, FALSE_POSITIVE, RESOLVED
    @Indexed
    private String status;

    @Indexed
    private String assignedTo;

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Resolution outcome: CONFIRMED_FRAUD, FALSE_POSITIVE, RESOLVED_NO_ACTION, etc.
    private String resolution;

    private String resolutionReason;
}
