package com.upimesh.reconciliation.service;

import com.upimesh.reconciliation.exception.InvalidStatementException;
import com.upimesh.reconciliation.model.dto.BankStatementEntry;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BankStatementParser {

    public List<BankStatementEntry> parseStatement(List<Map<String, String>> rawData) {
        if (rawData == null) {
            throw new InvalidStatementException("Raw statement data cannot be null");
        }

        List<BankStatementEntry> entries = new ArrayList<>();

        for (int i = 0; i < rawData.size(); i++) {
            Map<String, String> row = rawData.get(i);
            String bankRefNumber = row.get("bankRefNumber");
            String txnDateStr = row.get("transactionDate");
            String amountStr = row.get("amount");
            String type = row.get("type");
            String description = row.get("description");
            String merchantId = row.get("merchantId");
            String ourTransactionId = row.get("ourTransactionId");

            // 1. Validate bankRefNumber
            if (bankRefNumber == null || bankRefNumber.isBlank()) {
                throw new InvalidStatementException("Validation failed at entry " + i + ": bankRefNumber cannot be blank");
            }

            // 2. Validate transactionDate
            if (txnDateStr == null || txnDateStr.isBlank()) {
                throw new InvalidStatementException("Validation failed at entry " + i + ": transactionDate cannot be null");
            }
            LocalDate txnDate;
            try {
                txnDate = LocalDate.parse(txnDateStr.trim());
            } catch (DateTimeParseException e) {
                throw new InvalidStatementException("Validation failed at entry " + i + ": invalid transactionDate format: " + txnDateStr);
            }

            // 3. Validate amount
            if (amountStr == null || amountStr.isBlank()) {
                throw new InvalidStatementException("Validation failed at entry " + i + ": amount cannot be null");
            }
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr.trim());
            } catch (NumberFormatException e) {
                throw new InvalidStatementException("Validation failed at entry " + i + ": invalid amount format: " + amountStr);
            }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidStatementException("Validation failed at entry " + i + ": amount must be greater than zero");
            }

            // 4. Validate type
            if (type == null || (!"CREDIT".equalsIgnoreCase(type) && !"DEBIT".equalsIgnoreCase(type))) {
                throw new InvalidStatementException("Validation failed at entry " + i + ": type must be CREDIT or DEBIT");
            }

            entries.add(new BankStatementEntry(
                    bankRefNumber.trim(),
                    txnDate,
                    amount,
                    type.toUpperCase().trim(),
                    description,
                    merchantId,
                    ourTransactionId
            ));
        }

        return entries;
    }
}
