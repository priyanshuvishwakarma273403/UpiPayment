package com.fraudService.flink.model;

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
public class TemporalRiskSignal {

    private String signalId;
    private Long customerId;
    private String patternType; // VELOCITY_SPIKE_30S, RAPID_BENEFICIARY_2M, ATO_SEQUENCE_5M
    private String severity; // SUSPICIOUS, HIGH_RISK, REQUIRES_INVESTIGATION
    private int riskScoreAddition;
    private long windowStart;
    private long windowEnd;
    private List<String> contributingEventIds;
    private String eventSequenceDescription;
    private LocalDateTime timestamp;

}
