package com.fraudService.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    // Free text query searching across transaction ID, customer ID, case ID, device ID, IP, beneficiary, merchant, content
    private String query;

    // Filters
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private Double minRiskScore;
    private Double maxRiskScore;
    private String status; // OPEN, INVESTIGATING, ESCALATED, CONFIRMED_FRAUD, FALSE_POSITIVE, RESOLVED, SAFE, REVIEW, BLOCKED
    private String fraudType; // ACCOUNT_TAKEOVER, CARD_TESTING, MONEY_MULE, RAPID_FUND_MOVEMENT, BENEFICIARY_ABUSE, etc.

    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    // Pagination
    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;
}
