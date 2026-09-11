package com.upimesh.risk.rule;

import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.request.RiskScoringRequest;

/**
 * Strategy Interface for Risk Rules.
 * Each rule must be independently testable and return a RuleEvaluationResult.
 */
public interface RiskRule {

    String getRuleId();

    String getRuleName();

    RuleEvaluationResult evaluate(RiskScoringRequest request, RiskProfile profile);
}
