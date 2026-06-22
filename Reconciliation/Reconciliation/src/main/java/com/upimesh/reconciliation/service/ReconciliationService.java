package com.upimesh.reconciliation.service;

import com.upimesh.reconciliation.exception.*;
import com.upimesh.reconciliation.feign.NpciServiceClient;
import com.upimesh.reconciliation.feign.TransactionServiceClient;
import com.upimesh.reconciliation.feign.TransactionServiceClient.SystemTransactionDto;
import com.upimesh.reconciliation.model.dto.BankStatementEntry;
import com.upimesh.reconciliation.model.entity.ReconciliationDiscrepancy;
import com.upimesh.reconciliation.model.entity.ReconciliationReport;
import com.upimesh.reconciliation.model.enums.ReconciliationStatus;
import com.upimesh.reconciliation.model.enums.ReportStatus;
import com.upimesh.reconciliation.model.response.DiscrepancyResponse;
import com.upimesh.reconciliation.model.response.ReconciliationReportResponse;
import com.upimesh.reconciliation.model.response.ReconciliationSummaryResponse;
import com.upimesh.reconciliation.repository.ReconciliationDiscrepancyRepository;
import com.upimesh.reconciliation.repository.ReconciliationReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationService {

    private final ReconciliationReportRepository reportRepo;
    private final ReconciliationDiscrepancyRepository discrepancyRepo;
    
    private final TransactionServiceClient transactionClient;
    private final NpciServiceClient npciClient;
    
    private final ReconciliationEngine reconciliationEngine;

    @Value("${internal.service-key}")
    private String serviceKey;

    @Transactional
    public ReconciliationReportResponse runDailyReconciliation(LocalDate date) {
        log.info("Starting Daily Reconciliation for Date: {}", date);

        // 1. Check if report already exists for the given date (idempotency check)
        var existingReport = reportRepo.findByReconciliationDate(date);
        if (existingReport.isPresent()) {
            log.warn("Reconciliation report for date: {} already exists. Skipping.", date);
            return mapToReportResponse(existingReport.get());
        }

        // 2. Create ReconciliationReport with RUNNING status
        String reportId = "REP" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        ReconciliationReport report = ReconciliationReport.builder()
                .reportId(reportId)
                .reconciliationDate(date)
                .status(ReportStatus.RUNNING)
                .totalSystemTransactions(0)
                .totalBankTransactions(0)
                .matchedCount(0)
                .mismatchCount(0)
                .missingInBankCount(0)
                .missingInSystemCount(0)
                .duplicateCount(0)
                .totalSystemAmount(BigDecimal.ZERO)
                .totalBankAmount(BigDecimal.ZERO)
                .discrepancyAmount(BigDecimal.ZERO)
                .build();
        report = reportRepo.save(report);

        // 3. Fetch system transactions via Feign
        List<SystemTransactionDto> systemTxns = Collections.emptyList();
        try {
            log.info("Fetching system transactions for date: {}", date);
            systemTxns = transactionClient.getTransactionsByDate(serviceKey, date.toString());
        } catch (Exception e) {
            log.error("Failed to fetch system transactions for date: {}", date, e);
            report.setStatus(ReportStatus.FAILED);
            report.setNotes("Failed to fetch system transactions: " + e.getMessage());
            reportRepo.save(report);
            throw new SettlementProcessingException("Reconciliation failed: unable to fetch system transactions", e);
        }

        // 4. Generate Mock Bank Statement with 2% random discrepancies
        List<BankStatementEntry> bankEntries = generateMockBankStatement(systemTxns);

        // 5. Run Reconciliation Engine
        Map<ReconciliationStatus, List<ReconciliationDiscrepancy>> grouped = 
                reconciliationEngine.reconcile(systemTxns, bankEntries);

        // 6. Save Discrepancies (all except MATCHED)
        List<ReconciliationDiscrepancy> discrepanciesToSave = new ArrayList<>();
        for (Map.Entry<ReconciliationStatus, List<ReconciliationDiscrepancy>> entry : grouped.entrySet()) {
            if (entry.getKey() != ReconciliationStatus.MATCHED) {
                for (ReconciliationDiscrepancy disc : entry.getValue()) {
                    disc.setReportId(reportId);
                    discrepanciesToSave.add(disc);
                }
            }
        }
        discrepancyRepo.saveAll(discrepanciesToSave);

        // 7. Calculate Aggregates
        int matched = grouped.get(ReconciliationStatus.MATCHED).size();
        int mismatched = grouped.get(ReconciliationStatus.MISMATCH_AMOUNT).size();
        int missingInBank = grouped.get(ReconciliationStatus.MISSING_IN_BANK).size();
        int missingInSystem = grouped.get(ReconciliationStatus.MISSING_IN_SYSTEM).size();
        int duplicate = grouped.get(ReconciliationStatus.DUPLICATE_IN_BANK).size();

        BigDecimal systemAmount = systemTxns.stream()
                .filter(t -> "SUCCESS".equalsIgnoreCase(t.status()))
                .map(SystemTransactionDto::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal bankAmount = bankEntries.stream()
                .map(BankStatementEntry::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Discrepancy Amount is the sum of net differences in discrepancies
        BigDecimal discrepancyAmount = discrepanciesToSave.stream()
                .map(d -> d.getOurAmount().subtract(d.getBankAmount()).abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.setTotalSystemTransactions(systemTxns.size());
        report.setTotalBankTransactions(bankEntries.size());
        report.setMatchedCount(matched);
        report.setMismatchCount(mismatched);
        report.setMissingInBankCount(missingInBank);
        report.setMissingInSystemCount(missingInSystem);
        report.setDuplicateCount(duplicate);
        report.setTotalSystemAmount(systemAmount);
        report.setTotalBankAmount(bankAmount);
        report.setDiscrepancyAmount(discrepancyAmount);
        report.setStatus(ReportStatus.COMPLETED);
        report.setCompletedAt(LocalDateTime.now());
        
        double matchRate = systemTxns.isEmpty() ? 100.0 : ((double) matched / systemTxns.size()) * 100;
        report.setNotes(String.format("Reconciliation completed: %.2f%% matched. %d discrepancies found.", 
                matchRate, discrepanciesToSave.size()));

        report = reportRepo.save(report);
        log.info("Reconciliation completed successfully: reportId={} | {}% matched", reportId, String.format("%.2f", matchRate));

        return mapToReportResponse(report);
    }

    @Transactional
    public DiscrepancyResponse resolveDiscrepancy(String discrepancyId, String resolution, String resolvedBy) {
        log.info("Resolving discrepancy: {} | resolvedBy: {}", discrepancyId, resolvedBy);
        
        ReconciliationDiscrepancy discrepancy = discrepancyRepo.findByDiscrepancyId(discrepancyId)
                .orElseThrow(() -> new DiscrepancyNotFoundException("Discrepancy not found: " + discrepancyId));

        if (discrepancy.getStatus() == ReconciliationStatus.RESOLVED) {
            throw new IllegalArgumentException("Discrepancy is already resolved");
        }

        discrepancy.setStatus(ReconciliationStatus.RESOLVED);
        discrepancy.setResolution(resolution);
        discrepancy.setResolvedBy(resolvedBy);
        discrepancy.setResolvedAt(LocalDateTime.now());
        
        discrepancy = discrepancyRepo.save(discrepancy);
        log.info("Discrepancy successfully resolved: {}", discrepancyId);

        return mapToDiscrepancyResponse(discrepancy);
    }

    public ReconciliationReportResponse getReport(String reportId) {
        ReconciliationReport report = reportRepo.findByReportId(reportId)
                .orElseThrow(() -> new ReconciliationNotFoundException("Reconciliation report not found: " + reportId));
        return mapToReportResponse(report);
    }

    public List<DiscrepancyResponse> getDiscrepancies(String reportId) {
        return discrepancyRepo.findByReportId(reportId).stream()
                .map(this::mapToDiscrepancyResponse)
                .collect(Collectors.toList());
    }

    public ReconciliationSummaryResponse getSummary(String reportId) {
        ReconciliationReport report = reportRepo.findByReportId(reportId)
                .orElseThrow(() -> new ReconciliationNotFoundException("Reconciliation report not found: " + reportId));

        int totalDiscrepancies = report.getMismatchCount() + report.getMissingInBankCount() + 
                report.getMissingInSystemCount() + report.getDuplicateCount();

        double matchRate = report.getTotalSystemTransactions() == 0 ? 100.0 : 
                ((double) report.getMatchedCount() / report.getTotalSystemTransactions()) * 100.0;

        Map<ReconciliationStatus, Integer> breakdown = new HashMap<>();
        breakdown.put(ReconciliationStatus.MATCHED, report.getMatchedCount());
        breakdown.put(ReconciliationStatus.MISMATCH_AMOUNT, report.getMismatchCount());
        breakdown.put(ReconciliationStatus.MISSING_IN_BANK, report.getMissingInBankCount());
        breakdown.put(ReconciliationStatus.MISSING_IN_SYSTEM, report.getMissingInSystemCount());
        breakdown.put(ReconciliationStatus.DUPLICATE_IN_BANK, report.getDuplicateCount());

        return ReconciliationSummaryResponse.builder()
                .reportId(report.getReportId())
                .date(report.getReconciliationDate())
                .matchRate(Math.round(matchRate * 100.0) / 100.0)
                .totalDiscrepancies(totalDiscrepancies)
                .totalDiscrepancyAmount(report.getDiscrepancyAmount())
                .status(report.getStatus())
                .breakdown(breakdown)
                .build();
    }

    public List<DiscrepancyResponse> getUnresolvedDiscrepancies() {
        return discrepancyRepo.findUnresolvedDiscrepancies().stream()
                .map(this::mapToDiscrepancyResponse)
                .collect(Collectors.toList());
    }

    // ─── Excel Report Exporter using POI ───────────────────────────────────────

    public byte[] exportReportToExcel(String reportId) {
        ReconciliationReport report = reportRepo.findByReportId(reportId)
                .orElseThrow(() -> new ReconciliationNotFoundException("Reconciliation report not found: " + reportId));
        
        List<ReconciliationDiscrepancy> discrepancies = discrepancyRepo.findByReportId(reportId);

        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Create styles
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Sheet 1: Summary Report
            Sheet summarySheet = workbook.createSheet("Summary");
            Row titleRow = summarySheet.createRow(0);
            titleRow.createCell(0).setCellValue("Reconciliation Audit Report Summary");
            
            Row rIdRow = summarySheet.createRow(2);
            rIdRow.createCell(0).setCellValue("Report ID:");
            rIdRow.createCell(1).setCellValue(report.getReportId());
            
            Row dateRow = summarySheet.createRow(3);
            dateRow.createCell(0).setCellValue("Reconciliation Date:");
            dateRow.createCell(1).setCellValue(report.getReconciliationDate().toString());

            Row statusRow = summarySheet.createRow(4);
            statusRow.createCell(0).setCellValue("Status:");
            statusRow.createCell(1).setCellValue(report.getStatus().name());

            Row statsHeader = summarySheet.createRow(6);
            statsHeader.createCell(0).setCellValue("Metric");
            statsHeader.createCell(1).setCellValue("Value");
            statsHeader.getCell(0).setCellStyle(headerStyle);
            statsHeader.getCell(1).setCellStyle(headerStyle);

            String[][] metrics = {
                    {"Total System Transactions", String.valueOf(report.getTotalSystemTransactions())},
                    {"Total Bank Transactions", String.valueOf(report.getTotalBankTransactions())},
                    {"Matched Count", String.valueOf(report.getMatchedCount())},
                    {"Mismatch Amount Count", String.valueOf(report.getMismatchCount())},
                    {"Missing In Bank Count", String.valueOf(report.getMissingInBankCount())},
                    {"Missing In System Count", String.valueOf(report.getMissingInSystemCount())},
                    {"Duplicate In Bank Count", String.valueOf(report.getDuplicateCount())},
                    {"Total System Amount", "₹" + report.getTotalSystemAmount()},
                    {"Total Bank Amount", "₹" + report.getTotalBankAmount()},
                    {"Total Discrepancy Amount", "₹" + report.getDiscrepancyAmount()}
            };

            for (int i = 0; i < metrics.length; i++) {
                Row row = summarySheet.createRow(7 + i);
                row.createCell(0).setCellValue(metrics[i][0]);
                row.createCell(1).setCellValue(metrics[i][1]);
            }
            summarySheet.autoSizeColumn(0);
            summarySheet.autoSizeColumn(1);

            // Sheet 2: Discrepancies List
            Sheet discSheet = workbook.createSheet("Discrepancies");
            Row discHeader = discSheet.createRow(0);
            String[] headers = {
                    "Discrepancy ID", "Transaction ID", "Bank Ref Number", 
                    "System Amount", "Bank Amount", "Status", "Description", 
                    "Resolution Notes", "Resolved By", "Resolved At"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = discHeader.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (ReconciliationDiscrepancy d : discrepancies) {
                Row row = discSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.getDiscrepancyId());
                row.createCell(1).setCellValue(d.getTransactionId() != null ? d.getTransactionId() : "N/A");
                row.createCell(2).setCellValue(d.getBankReferenceNumber() != null ? d.getBankReferenceNumber() : "N/A");
                row.createCell(3).setCellValue(d.getOurAmount().doubleValue());
                row.createCell(4).setCellValue(d.getBankAmount().doubleValue());
                row.createCell(5).setCellValue(d.getStatus().name());
                row.createCell(6).setCellValue(d.getDescription());
                row.createCell(7).setCellValue(d.getResolution() != null ? d.getResolution() : "UNRESOLVED");
                row.createCell(8).setCellValue(d.getResolvedBy() != null ? d.getResolvedBy() : "");
                row.createCell(9).setCellValue(d.getResolvedAt() != null ? d.getResolvedAt().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                discSheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate POI Excel workbook for report: {}", reportId, e);
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }

    // ─── Mock Statement Generation ─────────────────────────────────────────────

    private List<BankStatementEntry> generateMockBankStatement(List<SystemTransactionDto> systemTxns) {
        List<BankStatementEntry> entries = new ArrayList<>();
        Random random = new Random();

        for (SystemTransactionDto txn : systemTxns) {
            if (!"SUCCESS".equalsIgnoreCase(txn.status())) {
                continue;
            }

            String bankRef = (txn.rrn() != null && !txn.rrn().isBlank()) ? txn.rrn() : 
                    ((txn.npciTransactionId() != null && !txn.npciTransactionId().isBlank()) ? txn.npciTransactionId() : txn.transactionId());

            double randVal = random.nextDouble();

            if (randVal < 0.02) {
                // 2% Random Discrepancies
                int type = random.nextInt(4);
                switch (type) {
                    case 0 -> {
                        // 1. Amount mismatch (Bank shows different amount)
                        BigDecimal badAmt = txn.amount().add(BigDecimal.valueOf(1.00));
                        entries.add(new BankStatementEntry(
                                bankRef, txn.completedAt().toLocalDate(), badAmt, "DEBIT", "UPI payment amount mismatch", txn.receiverUpiId(), txn.transactionId()));
                    }
                    case 1 -> {
                        // 2. Duplicate debits (Bank charged twice)
                        entries.add(new BankStatementEntry(
                                bankRef, txn.completedAt().toLocalDate(), txn.amount(), "DEBIT", "UPI payment", txn.receiverUpiId(), txn.transactionId()));
                        entries.add(new BankStatementEntry(
                                bankRef, txn.completedAt().toLocalDate(), txn.amount(), "DEBIT", "UPI duplicate charge", txn.receiverUpiId(), txn.transactionId()));
                    }
                    case 2 -> {
                        // 3. Missing in bank (Do not add bank entry to statement)
                        log.info("Mock: Skipping transaction {} in bank statement (Missing in Bank)", txn.transactionId());
                    }
                    case 3 -> {
                        // 4. Missing in system (Orphan charge)
                        // Add correct entry
                        entries.add(new BankStatementEntry(
                                bankRef, txn.completedAt().toLocalDate(), txn.amount(), "DEBIT", "UPI payment", txn.receiverUpiId(), txn.transactionId()));
                        // Plus an orphan entry
                        String orphanRef = "MOK" + (1000000000L + random.nextInt(900000000));
                        entries.add(new BankStatementEntry(
                                orphanRef, txn.completedAt().toLocalDate(), BigDecimal.valueOf(500), "DEBIT", "Orphan charge", txn.receiverUpiId(), null));
                    }
                }
            } else {
                // 98% Correct Matching Entries
                entries.add(new BankStatementEntry(
                        bankRef, txn.completedAt().toLocalDate(), txn.amount(), "DEBIT", "UPI payment", txn.receiverUpiId(), txn.transactionId()));
            }
        }

        // Add 1 extra orphan transaction always for testing engine logic if statement empty
        if (systemTxns.isEmpty()) {
            entries.add(new BankStatementEntry(
                    "MOK999999", LocalDate.now(), BigDecimal.valueOf(100), "DEBIT", "Orphan test", "receiver@upi", null));
        }

        return entries;
    }

    private ReconciliationReportResponse mapToReportResponse(ReconciliationReport report) {
        return ReconciliationReportResponse.builder()
                .reportId(report.getReportId())
                .reconciliationDate(report.getReconciliationDate())
                .totalSystemTransactions(report.getTotalSystemTransactions())
                .totalBankTransactions(report.getTotalBankTransactions())
                .matchedCount(report.getMatchedCount())
                .mismatchCount(report.getMismatchCount())
                .missingInBankCount(report.getMissingInBankCount())
                .missingInSystemCount(report.getMissingInSystemCount())
                .duplicateCount(report.getDuplicateCount())
                .totalSystemAmount(report.getTotalSystemAmount())
                .totalBankAmount(report.getTotalBankAmount())
                .discrepancyAmount(report.getDiscrepancyAmount())
                .status(report.getStatus())
                .notes(report.getNotes())
                .createdAt(report.getCreatedAt())
                .completedAt(report.getCompletedAt())
                .build();
    }

    private DiscrepancyResponse mapToDiscrepancyResponse(ReconciliationDiscrepancy d) {
        return DiscrepancyResponse.builder()
                .discrepancyId(d.getDiscrepancyId())
                .reportId(d.getReportId())
                .transactionId(d.getTransactionId())
                .bankReferenceNumber(d.getBankReferenceNumber())
                .ourAmount(d.getOurAmount())
                .bankAmount(d.getBankAmount())
                .status(d.getStatus())
                .description(d.getDescription())
                .resolution(d.getResolution())
                .resolvedAt(d.getResolvedAt())
                .resolvedBy(d.getResolvedBy())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
