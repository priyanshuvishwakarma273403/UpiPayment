package com.upimesh.reconciliation.service;

import com.upimesh.reconciliation.feign.TransactionServiceClient.SystemTransactionDto;
import com.upimesh.reconciliation.model.dto.BankStatementEntry;
import com.upimesh.reconciliation.model.entity.ReconciliationDiscrepancy;
import com.upimesh.reconciliation.model.enums.ReconciliationStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ReconciliationEngine {

    public Map<ReconciliationStatus, List<ReconciliationDiscrepancy>> reconcile(
            List<SystemTransactionDto> systemTxns, 
            List<BankStatementEntry> bankEntries) {

        Map<ReconciliationStatus, List<ReconciliationDiscrepancy>> result = new HashMap<>();
        for (ReconciliationStatus status : ReconciliationStatus.values()) {
            result.put(status, new ArrayList<>());
        }

        if (systemTxns == null) systemTxns = Collections.emptyList();
        if (bankEntries == null) bankEntries = Collections.emptyList();

        // 1. Build Lookup Maps for System Transactions
        Map<String, SystemTransactionDto> systemByRrn = new HashMap<>();
        Map<String, SystemTransactionDto> systemByNpciId = new HashMap<>();
        Map<String, SystemTransactionDto> systemById = new HashMap<>();

        for (SystemTransactionDto txn : systemTxns) {
            if (txn.rrn() != null && !txn.rrn().isBlank()) {
                systemByRrn.put(txn.rrn().trim(), txn);
            }
            if (txn.npciTransactionId() != null && !txn.npciTransactionId().isBlank()) {
                systemByNpciId.put(txn.npciTransactionId().trim(), txn);
            }
            if (txn.transactionId() != null && !txn.transactionId().isBlank()) {
                systemById.put(txn.transactionId().trim(), txn);
            }
        }

        // 2. Detect Duplicate Bank References
        Map<String, List<BankStatementEntry>> bankRefGroups = new HashMap<>();
        for (BankStatementEntry entry : bankEntries) {
            if (entry.bankRefNumber() != null && !entry.bankRefNumber().isBlank()) {
                bankRefGroups.computeIfAbsent(entry.bankRefNumber().trim(), k -> new ArrayList<>()).add(entry);
            }
        }

        Set<String> duplicateBankRefs = new HashSet<>();
        for (Map.Entry<String, List<BankStatementEntry>> groupEntry : bankRefGroups.entrySet()) {
            if (groupEntry.getValue().size() > 1) {
                duplicateBankRefs.add(groupEntry.getKey());
                for (BankStatementEntry entry : groupEntry.getValue()) {
                    ReconciliationDiscrepancy discrepancy = ReconciliationDiscrepancy.builder()
                            .discrepancyId("DIS" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase())
                            .transactionId(entry.ourTransactionId())
                            .bankReferenceNumber(entry.bankRefNumber())
                            .ourAmount(BigDecimal.ZERO)
                            .bankAmount(entry.amount())
                            .status(ReconciliationStatus.DUPLICATE_IN_BANK)
                            .description("Duplicate debit found in bank statement for reference: " + entry.bankRefNumber())
                            .build();
                    result.get(ReconciliationStatus.DUPLICATE_IN_BANK).add(discrepancy);
                }
            }
        }

        // 3. Reconcile System Transactions (status must be SUCCESS)
        Set<String> matchedBankRefNumbers = new HashSet<>();
        for (SystemTransactionDto systemTxn : systemTxns) {
            if (!"SUCCESS".equalsIgnoreCase(systemTxn.status())) {
                continue;
            }

            // Find matching bank statement entry
            BankStatementEntry matchedEntry = null;
            if (systemTxn.rrn() != null && bankRefGroups.containsKey(systemTxn.rrn().trim())) {
                matchedEntry = bankRefGroups.get(systemTxn.rrn().trim()).get(0);
            } else if (systemTxn.npciTransactionId() != null && bankRefGroups.containsKey(systemTxn.npciTransactionId().trim())) {
                matchedEntry = bankRefGroups.get(systemTxn.npciTransactionId().trim()).get(0);
            } else if (systemTxn.transactionId() != null && bankRefGroups.containsKey(systemTxn.transactionId().trim())) {
                matchedEntry = bankRefGroups.get(systemTxn.transactionId().trim()).get(0);
            }

            if (matchedEntry != null) {
                matchedBankRefNumbers.add(matchedEntry.bankRefNumber());
                
                // Compare Amounts
                if (systemTxn.amount().compareTo(matchedEntry.amount()) == 0) {
                    ReconciliationDiscrepancy matchRecord = ReconciliationDiscrepancy.builder()
                            .discrepancyId("DIS" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase())
                            .transactionId(systemTxn.transactionId())
                            .bankReferenceNumber(matchedEntry.bankRefNumber())
                            .ourAmount(systemTxn.amount())
                            .bankAmount(matchedEntry.amount())
                            .status(ReconciliationStatus.MATCHED)
                            .description("Reconciliation matched successfully")
                            .build();
                    result.get(ReconciliationStatus.MATCHED).add(matchRecord);
                } else {
                    ReconciliationDiscrepancy mismatchRecord = ReconciliationDiscrepancy.builder()
                            .discrepancyId("DIS" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase())
                            .transactionId(systemTxn.transactionId())
                            .bankReferenceNumber(matchedEntry.bankRefNumber())
                            .ourAmount(systemTxn.amount())
                            .bankAmount(matchedEntry.amount())
                            .status(ReconciliationStatus.MISMATCH_AMOUNT)
                            .description("Amount mismatch! System: ₹" + systemTxn.amount() + ", Bank: ₹" + matchedEntry.amount())
                            .build();
                    result.get(ReconciliationStatus.MISMATCH_AMOUNT).add(mismatchRecord);
                }
            } else {
                // Not found in bank statement
                ReconciliationDiscrepancy missingInBank = ReconciliationDiscrepancy.builder()
                        .discrepancyId("DIS" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase())
                        .transactionId(systemTxn.transactionId())
                        .bankReferenceNumber(systemTxn.rrn())
                        .ourAmount(systemTxn.amount())
                        .bankAmount(BigDecimal.ZERO)
                        .status(ReconciliationStatus.MISSING_IN_BANK)
                        .description("Transaction marked success in system but missing from bank statement")
                        .build();
                result.get(ReconciliationStatus.MISSING_IN_BANK).add(missingInBank);
            }
        }

        // 4. Reconcile Bank Statement Entries to find orphan transactions
        for (BankStatementEntry entry : bankEntries) {
            // Skip duplicates and already matched entries
            if (duplicateBankRefs.contains(entry.bankRefNumber()) || matchedBankRefNumbers.contains(entry.bankRefNumber())) {
                continue;
            }

            // Check if there is any system txn matching this entry
            SystemTransactionDto systemTxn = null;
            if (entry.bankRefNumber() != null) {
                if (systemByRrn.containsKey(entry.bankRefNumber())) {
                    systemTxn = systemByRrn.get(entry.bankRefNumber());
                } else if (systemByNpciId.containsKey(entry.bankRefNumber())) {
                    systemTxn = systemByNpciId.get(entry.bankRefNumber());
                }
            }
            if (systemTxn == null && entry.ourTransactionId() != null && systemById.containsKey(entry.ourTransactionId())) {
                systemTxn = systemById.get(entry.ourTransactionId());
            }

            if (systemTxn == null) {
                // Orphan charge: in bank but not in our system (or failed in our system)
                ReconciliationDiscrepancy missingInSystem = ReconciliationDiscrepancy.builder()
                        .discrepancyId("DIS" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase())
                        .transactionId(entry.ourTransactionId())
                        .bankReferenceNumber(entry.bankRefNumber())
                        .ourAmount(BigDecimal.ZERO)
                        .bankAmount(entry.amount())
                        .status(ReconciliationStatus.MISSING_IN_SYSTEM)
                        .description("Orphan charge: Bank shows transaction but not found/recorded as success in system")
                        .build();
                result.get(ReconciliationStatus.MISSING_IN_SYSTEM).add(missingInSystem);
            }
        }

        return result;
    }
}
