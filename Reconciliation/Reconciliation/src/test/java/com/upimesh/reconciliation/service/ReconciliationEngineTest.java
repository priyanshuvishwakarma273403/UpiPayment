package com.upimesh.reconciliation.service;

import com.upimesh.reconciliation.feign.TransactionServiceClient.SystemTransactionDto;
import com.upimesh.reconciliation.model.dto.BankStatementEntry;
import com.upimesh.reconciliation.model.entity.ReconciliationDiscrepancy;
import com.upimesh.reconciliation.model.enums.ReconciliationStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReconciliationEngineTest {

    private final ReconciliationEngine engine = new ReconciliationEngine();

    @Test
    public void allMatchedPerfectly() {
        SystemTransactionDto s1 = new SystemTransactionDto(
                "TX1", "sender@upi", "receiver@upi", BigDecimal.valueOf(100.00), "SUCCESS", "NPCI1", "RRN1", LocalDateTime.now());
        
        BankStatementEntry b1 = new BankStatementEntry(
                "RRN1", LocalDate.now(), BigDecimal.valueOf(100.00), "DEBIT", "UPI pay", "receiver@upi", "TX1");

        Map<ReconciliationStatus, List<ReconciliationDiscrepancy>> result = 
                engine.reconcile(Collections.singletonList(s1), Collections.singletonList(b1));

        assertNotNull(result);
        assertEquals(1, result.get(ReconciliationStatus.MATCHED).size());
        assertEquals(0, result.get(ReconciliationStatus.MISMATCH_AMOUNT).size());
        assertEquals(0, result.get(ReconciliationStatus.MISSING_IN_BANK).size());
        assertEquals(0, result.get(ReconciliationStatus.MISSING_IN_SYSTEM).size());
    }

    @Test
    public void detectAmountMismatch() {
        SystemTransactionDto s1 = new SystemTransactionDto(
                "TX1", "sender@upi", "receiver@upi", BigDecimal.valueOf(100.00), "SUCCESS", "NPCI1", "RRN1", LocalDateTime.now());
        
        BankStatementEntry b1 = new BankStatementEntry(
                "RRN1", LocalDate.now(), BigDecimal.valueOf(101.00), "DEBIT", "UPI pay", "receiver@upi", "TX1");

        Map<ReconciliationStatus, List<ReconciliationDiscrepancy>> result = 
                engine.reconcile(Collections.singletonList(s1), Collections.singletonList(b1));

        assertNotNull(result);
        assertEquals(0, result.get(ReconciliationStatus.MATCHED).size());
        assertEquals(1, result.get(ReconciliationStatus.MISMATCH_AMOUNT).size());
        
        ReconciliationDiscrepancy disc = result.get(ReconciliationStatus.MISMATCH_AMOUNT).get(0);
        assertEquals(BigDecimal.valueOf(100.00), disc.getOurAmount());
        assertEquals(BigDecimal.valueOf(101.00), disc.getBankAmount());
    }

    @Test
    public void detectMissingInBank() {
        SystemTransactionDto s1 = new SystemTransactionDto(
                "TX1", "sender@upi", "receiver@upi", BigDecimal.valueOf(100.00), "SUCCESS", "NPCI1", "RRN1", LocalDateTime.now());

        Map<ReconciliationStatus, List<ReconciliationDiscrepancy>> result = 
                engine.reconcile(Collections.singletonList(s1), Collections.emptyList());

        assertNotNull(result);
        assertEquals(1, result.get(ReconciliationStatus.MISSING_IN_BANK).size());
        
        ReconciliationDiscrepancy disc = result.get(ReconciliationStatus.MISSING_IN_BANK).get(0);
        assertEquals(BigDecimal.valueOf(100.00), disc.getOurAmount());
        assertEquals(BigDecimal.ZERO, disc.getBankAmount());
    }

    @Test
    public void detectMissingInSystem() {
        BankStatementEntry b1 = new BankStatementEntry(
                "RRN_ORPHAN", LocalDate.now(), BigDecimal.valueOf(100.00), "DEBIT", "Orphan pay", "receiver@upi", null);

        Map<ReconciliationStatus, List<ReconciliationDiscrepancy>> result = 
                engine.reconcile(Collections.emptyList(), Collections.singletonList(b1));

        assertNotNull(result);
        assertEquals(1, result.get(ReconciliationStatus.MISSING_IN_SYSTEM).size());
        
        ReconciliationDiscrepancy disc = result.get(ReconciliationStatus.MISSING_IN_SYSTEM).get(0);
        assertEquals(BigDecimal.ZERO, disc.getOurAmount());
        assertEquals(BigDecimal.valueOf(100.00), disc.getBankAmount());
    }

    @Test
    public void detectDuplicateBankEntry() {
        SystemTransactionDto s1 = new SystemTransactionDto(
                "TX1", "sender@upi", "receiver@upi", BigDecimal.valueOf(100.00), "SUCCESS", "NPCI1", "RRN1", LocalDateTime.now());
        
        BankStatementEntry b1 = new BankStatementEntry(
                "RRN1", LocalDate.now(), BigDecimal.valueOf(100.00), "DEBIT", "UPI pay", "receiver@upi", "TX1");
        BankStatementEntry b2 = new BankStatementEntry(
                "RRN1", LocalDate.now(), BigDecimal.valueOf(100.00), "DEBIT", "Duplicate pay", "receiver@upi", "TX1");

        Map<ReconciliationStatus, List<ReconciliationDiscrepancy>> result = 
                engine.reconcile(Collections.singletonList(s1), Arrays.asList(b1, b2));

        assertNotNull(result);
        assertEquals(2, result.get(ReconciliationStatus.DUPLICATE_IN_BANK).size());
    }

    @Test
    public void mixedScenario() {
        SystemTransactionDto s1 = new SystemTransactionDto(
                "TX1", "sender@upi", "receiver@upi", BigDecimal.valueOf(100.00), "SUCCESS", "NPCI1", "RRN1", LocalDateTime.now());
        SystemTransactionDto s2 = new SystemTransactionDto(
                "TX2", "sender@upi", "receiver@upi", BigDecimal.valueOf(200.00), "SUCCESS", "NPCI2", "RRN2", LocalDateTime.now());
        
        BankStatementEntry b1 = new BankStatementEntry(
                "RRN1", LocalDate.now(), BigDecimal.valueOf(100.00), "DEBIT", "UPI pay", "receiver@upi", "TX1");
        BankStatementEntry bOrphan = new BankStatementEntry(
                "RRN_ORPHAN", LocalDate.now(), BigDecimal.valueOf(500.00), "DEBIT", "Orphan pay", "receiver@upi", null);

        Map<ReconciliationStatus, List<ReconciliationDiscrepancy>> result = 
                engine.reconcile(Arrays.asList(s1, s2), Arrays.asList(b1, bOrphan));

        assertNotNull(result);
        assertEquals(1, result.get(ReconciliationStatus.MATCHED).size()); // s1 matched b1
        assertEquals(1, result.get(ReconciliationStatus.MISSING_IN_BANK).size()); // s2 missing in bank
        assertEquals(1, result.get(ReconciliationStatus.MISSING_IN_SYSTEM).size()); // bOrphan missing in system
    }
}
