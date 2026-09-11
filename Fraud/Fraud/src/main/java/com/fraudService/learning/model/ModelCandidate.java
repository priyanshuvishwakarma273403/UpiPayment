package com.fraudService.learning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Metadata DTO for candidate ML models created during continuous learning pipeline runs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelCandidate {

    private String runId;
    private String modelVersion;
    private String datasetVersion;
    private String trainingTimestamp;

    /**
     * Status lifecycle: PENDING_APPROVAL -> APPROVED -> DEPLOYED (or REJECTED)
     */
    private String status;

    private String approvedBy;
    private String approvedAt;
    private Map<String, Object> metrics;
    private Map<String, Object> featureValidation;
}
