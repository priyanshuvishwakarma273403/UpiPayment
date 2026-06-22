package com.upimesh.reconciliation.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BankStatementEntry(
        String bankRefNumber,
        LocalDate transactionDate,
        BigDecimal amount,
        String type, // "CREDIT" or "DEBIT"
        String description,
        String merchantId,
        String ourTransactionId // Can be null
) {}
