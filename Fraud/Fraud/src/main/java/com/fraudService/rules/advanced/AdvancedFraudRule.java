package com.fraudService.rules.advanced;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudSignal;

/**
 * Strategy interface for Advanced Fraud Intelligence Rules.
 */
public interface AdvancedFraudRule {

    String getSignalId();

    String getSignalName();

    String getCategory();

    FraudSignal evaluateSignal(FraudCheckRequest request);
}
