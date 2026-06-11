package com.npci.model.enums;

public enum TransactionStatus {
    INITIATED,      // Just created, not sent to NPCI yet
    PENDING,        // Sent to NPCI, waiting for response
    SUCCESS,        // NPCI confirmed success
    FAILED,         // NPCI returned failure
    TIMEOUT,        // No response from NPCI within threshold
    REVERSED,       // Successfully reversed/refunded
    REFUND_PENDING, // Refund initiated, not complete
    DECLINED        // Bank declined (insufficient funds, limits etc)
}
