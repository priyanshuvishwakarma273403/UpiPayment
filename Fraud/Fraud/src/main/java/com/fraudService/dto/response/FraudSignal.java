package com.fraudService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Single detected fraud signal payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudSignal {

    private String signalId;
    private String signalName;
    private String category;
    private double scoreContribution; // 0.0 to 1.0 (e.g. 0.35)
    private double confidence; // 0.0 to 1.0 (e.g. 0.90)
    private boolean triggered;
    private String explanation;

    @Builder.Default
    private Map<String, Object> evidenceMap = new HashMap<>();

    public static FraudSignal pass(String signalId, String signalName, String category) {
        return FraudSignal.builder()
                .signalId(signalId)
                .signalName(signalName)
                .category(category)
                .scoreContribution(0.0)
                .confidence(0.0)
                .triggered(false)
                .explanation("Clean - no anomaly detected")
                .build();
    }

    public static FraudSignal flag(String signalId, String signalName, String category, double score, double confidence, String explanation, Map<String, Object> evidenceMap) {
        return FraudSignal.builder()
                .signalId(signalId)
                .signalName(signalName)
                .category(category)
                .scoreContribution(score)
                .confidence(confidence)
                .triggered(true)
                .explanation(explanation)
                .evidenceMap(evidenceMap != null ? evidenceMap : new HashMap<>())
                .build();
    }
}
