package com.upimesh.reconciliation.service;

import com.upimesh.reconciliation.feign.NpciServiceClient;
import com.upimesh.reconciliation.feign.TransactionServiceClient;
import com.upimesh.reconciliation.feign.TransactionServiceClient.SystemTransactionDto;
import com.upimesh.reconciliation.model.entity.ReconciliationDiscrepancy;
import com.upimesh.reconciliation.model.entity.ReconciliationReport;
import com.upimesh.reconciliation.model.enums.ReconciliationStatus;
import com.upimesh.reconciliation.model.enums.ReportStatus;
import com.upimesh.reconciliation.model.response.DiscrepancyResponse;
import com.upimesh.reconciliation.model.response.ReconciliationReportResponse;
import com.upimesh.reconciliation.repository.ReconciliationDiscrepancyRepository;
import com.upimesh.reconciliation.repository.ReconciliationReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReconciliationServiceTest {

    @Mock
    private ReconciliationReportRepository reportRepo;

    @Mock
    private ReconciliationDiscrepancyRepository discrepancyRepo;

    @Mock
    private TransactionServiceClient transactionClient;

    @Mock
    private NpciServiceClient npciClient;

    @Mock
    private ReconciliationEngine reconciliationEngine;

    @InjectMocks
    private ReconciliationService reconciliationService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(reconciliationService, "serviceKey", "testKey");
    }

    @Test
    public void runReconciliationCreatesReport() {
        LocalDate date = LocalDate.now();
        SystemTransactionDto systemTxn = new SystemTransactionDto(
                "TX1", "sender@upi", "receiver@upi", BigDecimal.valueOf(100.00), "SUCCESS", "NPCI1", "RRN1", LocalDateTime.now());

        when(reportRepo.findByReconciliationDate(date)).thenReturn(Optional.empty());
        when(reportRepo.save(any(ReconciliationReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        when(transactionClient.getTransactionsByDate(eq("testKey"), eq(date.toString())))
                .thenReturn(Collections.singletonList(systemTxn));

        Map<ReconciliationStatus, List<ReconciliationDiscrepancy>> engineResult = new HashMap<>();
        for (ReconciliationStatus status : ReconciliationStatus.values()) {
            engineResult.put(status, Collections.emptyList());
        }

        when(reconciliationEngine.reconcile(any(), any())).thenReturn(engineResult);

        ReconciliationReportResponse response = reconciliationService.runDailyReconciliation(date);

        assertNotNull(response);
        assertEquals(ReportStatus.COMPLETED, response.getStatus());
        assertEquals(1, response.getTotalSystemTransactions());
        
        verify(reportRepo, times(2)).save(any(ReconciliationReport.class));
    }

    @Test
    public void skipsDuplicateReport() {
        LocalDate date = LocalDate.now();
        ReconciliationReport existing = ReconciliationReport.builder()
                .reportId("REP123")
                .reconciliationDate(date)
                .status(ReportStatus.COMPLETED)
                .build();

        when(reportRepo.findByReconciliationDate(date)).thenReturn(Optional.of(existing));

        ReconciliationReportResponse response = reconciliationService.runDailyReconciliation(date);

        assertNotNull(response);
        assertEquals("REP123", response.getReportId());
        assertEquals(ReportStatus.COMPLETED, response.getStatus());
        verifyNoInteractions(transactionClient, reconciliationEngine);
    }

    @Test
    public void resolveDiscrepancy() {
        ReconciliationDiscrepancy discrepancy = ReconciliationDiscrepancy.builder()
                .discrepancyId("DIS123")
                .status(ReconciliationStatus.MISSING_IN_BANK)
                .ourAmount(BigDecimal.valueOf(100.00))
                .bankAmount(BigDecimal.ZERO)
                .build();

        when(discrepancyRepo.findByDiscrepancyId("DIS123")).thenReturn(Optional.of(discrepancy));
        when(discrepancyRepo.save(any(ReconciliationDiscrepancy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiscrepancyResponse response = reconciliationService.resolveDiscrepancy("DIS123", "Customer confirmed charge", "Admin1");

        assertNotNull(response);
        assertEquals(ReconciliationStatus.RESOLVED, response.getStatus());
        assertEquals("Customer confirmed charge", response.getResolution());
        assertEquals("Admin1", response.getResolvedBy());
        assertNotNull(response.getResolvedAt());
    }
}
