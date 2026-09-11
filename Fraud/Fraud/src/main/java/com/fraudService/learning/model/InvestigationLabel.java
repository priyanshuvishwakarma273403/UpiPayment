package com.fraudService.learning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Investigator outcome label mapping case resolution to feature dataset.
 * Status values: CONFIRMED_FRAUD (1.0) or FALSE_POSITIVE (0.0).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationLabel {

    private String labelId;
    private String caseId;
    private String transactionId;
    private String customerId;

    /**
     * Label Status: CONFIRMED_FRAUD or FALSE_POSITIVE
     */
    private String status;

    /**
     * Numeric Label: 1.0 (CONFIRMED_FRAUD) or 0.0 (FALSE_POSITIVE)
     */
    private Double numericLabel;

    private Map<String, Double> featureVector;
    private LocalDateTime labeledAt;
    private String analystId;
}
