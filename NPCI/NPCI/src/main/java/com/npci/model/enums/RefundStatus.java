package com.npci.model.enums;

public enum RefundStatus {
    REQUESTED,  // Refund request raised
    PROCESSING, // Sent to NPCI
    COMPLETED,  // Money returned to user's account
    FAILED,     // Refund failed (retryable)
    REJECTED    // NPCI rejected refund (non-retryable)
}
