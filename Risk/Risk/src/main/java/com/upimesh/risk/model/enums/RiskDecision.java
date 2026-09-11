package com.upimesh.risk.model.enums;

/**
 * Risk Decision Categories for Financial Transactions
 */
public enum RiskDecision {
    ALLOW,     // Transaction proceeds normally
    MONITOR,   // Transaction proceeds with async auditing flag
    STEP_UP,   // Requires additional authentication (e.g. OTP / 2FA)
    REVIEW,    // Escalated to manual risk analyst queue
    BLOCK      // Transaction blocked immediately
}
