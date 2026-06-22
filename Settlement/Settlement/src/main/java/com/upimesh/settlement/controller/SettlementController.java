package com.upimesh.settlement.controller;

import com.upimesh.settlement.model.response.ApiResponse;
import com.upimesh.settlement.model.response.MerchantSettlementResponse;
import com.upimesh.settlement.model.response.SettlementBatchResponse;
import com.upimesh.settlement.model.response.SettlementReportResponse;
import com.upimesh.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/settlement")
@RequiredArgsConstructor
@Slf4j
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/run")
    public ApiResponse<SettlementBatchResponse> runManualSettlement() {
        log.info("REST request to manually trigger EOD daily settlement run");
        SettlementBatchResponse response = settlementService.runDailySettlement();
        return ApiResponse.success(response, "Settlement batch run triggered successfully");
    }

    @GetMapping("/batch/{batchId}")
    public ApiResponse<SettlementBatchResponse> getBatchDetails(@PathVariable String batchId) {
        log.info("REST request to fetch batch details for batchId: {}", batchId);
        SettlementBatchResponse response = settlementService.getBatchDetails(batchId);
        return ApiResponse.success(response, "Batch details retrieved successfully");
    }

    @GetMapping("/batch/{batchId}/report")
    public ApiResponse<SettlementReportResponse> getSettlementReport(@PathVariable String batchId) {
        log.info("REST request to fetch settlement report for batchId: {}", batchId);
        SettlementReportResponse response = settlementService.getSettlementReport(batchId);
        return ApiResponse.success(response, "Settlement report retrieved successfully");
    }

    @GetMapping("/merchant/{merchantUpiId}")
    public ApiResponse<List<MerchantSettlementResponse>> getMerchantSettlements(
            @PathVariable String merchantUpiId) {
        log.info("REST request to fetch settlements for merchant VPA: {}", merchantUpiId);
        List<MerchantSettlementResponse> response = settlementService.getMerchantSettlements(merchantUpiId);
        return ApiResponse.success(response, "Merchant settlements retrieved successfully");
    }

    @GetMapping("/batch/date/{date}")
    public ApiResponse<SettlementBatchResponse> getBatchByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("REST request to fetch batch by date: {}", date);
        SettlementBatchResponse response = settlementService.getBatchByDate(date);
        return ApiResponse.success(response, "Batch details for date retrieved successfully");
    }
}
