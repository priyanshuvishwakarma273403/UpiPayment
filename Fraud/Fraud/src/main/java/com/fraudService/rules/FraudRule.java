package com.fraudService.rules;

import com.fraudService.dto.request.FraudCheckRequest;
import lombok.Data;

/**
 * Fraud Rule Interface - Strategy Pattern
 * Har rule ko yeh interface implement karna hai
 */
public interface FraudRule {

    RuleResult evaluate(FraudCheckRequest request);

    @Data
  class RuleResult{
      private final String ruleName;
      private final Decision decision;
      private final String reason;
      private final double riskScore; // 0.0 to 1.0

        public static RuleResult safe(String ruleName) {
            return new RuleResult(ruleName, Decision.SAFE, "No risk detected", 0.0);
        }

        public static RuleResult review(String ruleName, String reason, double score) {
            return new RuleResult(ruleName, Decision.REVIEW, reason, score);
        }

        public static RuleResult blocked(String ruleName, String reason) {
            return new RuleResult(ruleName, Decision.BLOCKED, reason, 1.0);
        }
  }

    enum Decision {
        SAFE, REVIEW, BLOCKED
    }
}
