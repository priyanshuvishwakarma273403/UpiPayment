package com.fraudService.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MlPredictionRequest {

    @JsonProperty("amount_deviation")
    private Double amountDeviation;

    @JsonProperty("velocity_1m")
    private Double velocity1m;

    @JsonProperty("device_age_days")
    private Double deviceAgeDays;

    @JsonProperty("account_age_days")
    private Double accountAgeDays;

    @JsonProperty("merchant_risk_score")
    private Double merchantRiskScore;

    @JsonProperty("beneficiary_history_count")
    private Double beneficiaryHistoryCount;

    @JsonProperty("behavioral_deviation")
    private Double behavioralDeviation;

    @JsonProperty("graph_cluster_density")
    private Double graphClusterDensity;
}
