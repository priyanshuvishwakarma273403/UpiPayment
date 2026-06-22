package com.upimesh.reconciliation.controller;

import com.upimesh.reconciliation.model.response.ApiResponse;
import com.upimesh.reconciliation.model.response.DiscrepancyResponse;
import com.upimesh.reconciliation.model.response.ReconciliationReportResponse;
import com.upimesh.reconciliation.model.response.ReconciliationSummaryResponse;
import com.upimesh.reconciliation.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reconciliation")
@RequiredArgsConstructor
@Slf4j
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @PostMapping("/run")
    public ApiResponse<ReconciliationReportResponse> runManualReconciliation(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("REST request to manually run reconciliation for date: {}", date);
        ReconciliationReportResponse response = reconciliationService.runDailyReconciliation(date);
        return ApiResponse.success(response, "Reconciliation successfully initiated");
    }

    @GetMapping("/report/{reportId}")
    public ApiResponse<ReconciliationReportResponse> getReport(@PathVariable String reportId) {
        log.info("REST request to get reconciliation report for reportId: {}", reportId);
        ReconciliationReportResponse response = reconciliationService.getReport(reportId);
        return ApiResponse.success(response, "Reconciliation report retrieved successfully");
    }

    @GetMapping("/report/{reportId}/discrepancies")
    public ApiResponse<List<DiscrepancyResponse>> getDiscrepancies(@PathVariable String reportId) {
        log.info("REST request to get discrepancies for reportId: {}", reportId);
        List<DiscrepancyResponse> response = reconciliationService.getDiscrepancies(reportId);
        return ApiResponse.success(response, "Discrepancies retrieved successfully");
    }

    @GetMapping("/report/{reportId}/summary")
    public ApiResponse<ReconciliationSummaryResponse> getSummary(@PathVariable String reportId) {
        log.info("REST request to get summary for reportId: {}", reportId);
        ReconciliationSummaryResponse response = reconciliationService.getSummary(reportId);
        return ApiResponse.success(response, "Reconciliation summary retrieved successfully");
    }

    @GetMapping("/discrepancies/unresolved")
    public ApiResponse<List<DiscrepancyResponse>> getUnresolvedDiscrepancies() {
        log.info("REST request to get all unresolved discrepancies");
        List<DiscrepancyResponse> response = reconciliationService.getUnresolvedDiscrepancies();
        return ApiResponse.success(response, "Unresolved discrepancies retrieved successfully");
    }

    @PutMapping("/discrepancy/{discrepancyId}/resolve")
    public ApiResponse<DiscrepancyResponse> resolveDiscrepancy(
            @PathVariable String discrepancyId,
            @RequestParam String resolution,
            @RequestParam String resolvedBy) {
        log.info("REST request to resolve discrepancy: {} | resolvedBy: {}", discrepancyId, resolvedBy);
        DiscrepancyResponse response = reconciliationService.resolveDiscrepancy(discrepancyId, resolution, resolvedBy);
        return ApiResponse.success(response, "Discrepancy successfully resolved");
    }

    @GetMapping("/report/{reportId}/export")
    public ResponseEntity<byte[]> exportReport(@PathVariable String reportId) {
        log.info("REST request to export reconciliation Excel report for reportId: {}", reportId);
        byte[] excelBytes = reconciliationService.exportReportToExcel(reportId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("Reconciliation_Report_" + reportId + ".xlsx")
                .build());
        
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}
