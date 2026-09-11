package com.fraudService.explanation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MlEvidenceItem {

    private double fraudProbability;
    private String modelVersion;
    private double confidence;
    private Map<String, Double> topFeatureContributions;
    private String datasetLabel;
}
