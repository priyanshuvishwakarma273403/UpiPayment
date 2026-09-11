package com.fraudService.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationState {

    private String investigationId;
    private Long targetCustomerId;
    private String targetEntityId;
    private String suspectedFraudType; // e.g. ACCOUNT_TAKEOVER
    private String currentStep;
    private int toolCallCount;
    private long startTimeMs;
    private long elapsedTimeMs;
    private boolean timedOut;

    // Structured State Collections
    private Map<String, Object> customerData;
    @Builder.Default
    private List<Map<String, Object>> transactions = new ArrayList<>();
    @Builder.Default
    private Set<String> devices = new LinkedHashSet<>();
    @Builder.Default
    private Set<String> ipAddresses = new LinkedHashSet<>();
    @Builder.Default
    private Map<String, Object> behavioralMetrics = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> riskData = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> graphData = new LinkedHashMap<>();
    @Builder.Default
    private List<Map<String, Object>> previousCases = new ArrayList<>();
    @Builder.Default
    private List<String> policyMatches = new ArrayList<>();
    @Builder.Default
    private List<String> missingEvidence = new ArrayList<>();
    @Builder.Default
    private List<String> stepLog = new ArrayList<>();

    private String status; // IN_PROGRESS, COMPLETED, FAILED, TIMED_OUT
}
