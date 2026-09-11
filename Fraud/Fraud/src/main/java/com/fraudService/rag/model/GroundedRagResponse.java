package com.fraudService.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroundedRagResponse {

    private String query;
    private boolean policyFound;
    private List<String> observedData;
    private List<String> retrievedPolicy;
    private List<String> modelPrediction;
    private List<String> aiRecommendation;
    private String formattedReport;
    private LocalDateTime timestamp;

}
