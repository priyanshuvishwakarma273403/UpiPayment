package com.fraudService.simulator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Analyst simulation output response containing rules triggered, risk breakdowns,
 * ML probability, final composite risk, decision, and explicit simulation mode label.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudSimulationResponse {

    @Builder.Default
    private boolean simulationMode = true;

    @Builder.Default
    private String modeLabel = "SIMULATION MODE";

    private List<RuleTriggerDetail> rulesTriggered;
    private double behavioralRisk;
    private double graphRisk;
    private double mlProbability;
    private double finalRisk;
    private String riskLevel;
    private String decision;
    private String explanation;
    private LocalDateTime simulatedAt;
    private long processingTimeMs;
}
