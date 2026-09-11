package com.fraudService.explanation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEvidenceItem {

    private String ruleId;
    private String ruleName;
    private String scoreContributionFormatted; // e.g. "+18", "+21", "+27", "+16"
    private double scoreContribution;
    private String severity;
    private String reason;
}
