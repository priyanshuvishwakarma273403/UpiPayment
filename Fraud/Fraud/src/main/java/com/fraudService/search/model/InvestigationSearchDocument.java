package com.fraudService.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "investigation_search_index")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationSearchDocument {

    @Id
    private String docId;

    // Type of entity indexed: TRANSACTION, CASE, LOG
    private String docType;

    private String transactionId;
    private String customerId;
    private String caseId;
    private String deviceId;
    private String ipAddress;
    private String beneficiaryUpiId;
    private String merchantId;
    private String fraudType;

    // Status: OPEN, INVESTIGATING, ESCALATED, CONFIRMED_FRAUD, FALSE_POSITIVE, RESOLVED, SAFE, REVIEW, BLOCKED
    private String status;

    private Double riskScore;
    private String riskLevel;

    private LocalDateTime timestamp;

    // Full-text content: reasons, notes, evidence details
    private String content;

    // Eventual consistency tracking
    private String indexingStatus; // INDEXED, FAILED_RETRY_QUEUED
    private LocalDateTime indexedAt;
    private int retryCount;
    private String lastError;
}
