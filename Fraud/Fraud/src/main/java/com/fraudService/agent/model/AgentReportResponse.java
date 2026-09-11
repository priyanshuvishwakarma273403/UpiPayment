package com.fraudService.agent.model;

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
public class AgentReportResponse {

    private String investigationId;
    private String targetEntity;
    private List<String> evidence;
    private List<String> timeline;
    private List<String> network;
    private List<String> risk;
    private String potentialFraudType;
    private String confidence; // e.g. HIGH (92%)
    private List<String> missingEvidence;
    private String recommendedAction; // BLOCK, FREEZE, ESCALATE
    private String formattedReport;
    private int toolCallsUsed;
    private long executionTimeMs;
    private LocalDateTime timestamp;

}
