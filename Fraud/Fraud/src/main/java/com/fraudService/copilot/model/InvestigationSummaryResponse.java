package com.fraudService.copilot.model;

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
public class InvestigationSummaryResponse {

    private String query;
    private String entityId;
    private boolean evidenceFound;
    private List<String> observedEvidence;
    private List<String> riskSignals;
    private List<String> transactionTimeline;
    private List<String> relatedEntities;
    private List<String> previousCases;
    private String assessment;
    private String recommendedNextStep;
    private String formattedSummary;
    private LocalDateTime timestamp;

}
