package com.npci.model.enums;

public enum TransactionType {
    P2P,        // Person to Person (user to user)
    P2M,        // Person to Merchant
    MANDATE,    // Recurring auto-debit
    REFUND,     // Refund of a previous transaction
    COLLECT     // Collect request (merchant pulls money)
}
