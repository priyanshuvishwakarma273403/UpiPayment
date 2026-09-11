package com.fraudService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MlPredictionResponse {

    private Double fraudProbability;
    private String modelVersion;
    private Double confidence;
    private Map<String, Double> featureContributions;
    private String datasetLabel;
}
