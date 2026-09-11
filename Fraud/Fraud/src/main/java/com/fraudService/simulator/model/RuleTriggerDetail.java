package com.fraudService.simulator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Details of a rule triggered during simulation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleTriggerDetail {

    private String ruleId;
    private String ruleName;
    private String category;
    private double scoreContribution;
    private String reason;
}
