package com.npci.model.enums;

public enum MandateStatus {
    CREATED,    // Mandate created, not yet approved by user's bank
    ACTIVE,     // User approved, ready to auto-debit
    PAUSED,     // Temporarily paused
    REVOKED,    // Cancelled permanently
    EXPIRED     // Past end date
}
