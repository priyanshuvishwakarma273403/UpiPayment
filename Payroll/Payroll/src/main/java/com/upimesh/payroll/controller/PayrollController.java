package com.upimesh.payroll.controller;

import com.upimesh.payroll.model.entity.PayrollBatch;
import com.upimesh.payroll.model.request.CreatePayrollBatchRequest;
import com.upimesh.payroll.model.response.ApiResponse;
import com.upimesh.payroll.model.response.PayrollBatchResponse;
import com.upimesh.payroll.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payroll")
@RequiredArgsConstructor
@Slf4j
public class PayrollController {

    private final PayrollService payrollService;

    /**
     * POST /payroll/batch/create
     * Creates a new company payroll batch and calculates deductions.
     */
    @PostMapping("/batch/create")
    public ResponseEntity<ApiResponse<PayrollBatchResponse>> createPayrollBatch(
            @Valid @RequestBody CreatePayrollBatchRequest request) {
        log.info("Received request to create payroll batch for company: {}", request.getCompanyId());
        PayrollBatchResponse response = payrollService.createPayrollBatch(
                request.getCompanyId(),
                request.getCompanyUpiId(),
                request.getEmployees(),
                request.getMonth()
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Payroll batch created as DRAFT successfully"));
    }

    /**
     * POST /payroll/batch/{id}/validate
     * Validates employee bank accounts via the bank-gateway service.
     */
    @PostMapping("/batch/{id}/validate")
    public ResponseEntity<ApiResponse<PayrollBatch>> validateBatch(
            @PathVariable("id") String batchId) {
        log.info("Received request to validate payroll batch: {}", batchId);
        PayrollBatch batch = payrollService.validateBatch(batchId);
        return ResponseEntity.ok(ApiResponse.success(batch, "Payroll batch bank accounts validated successfully"));
    }

    /**
     * POST /payroll/batch/{id}/process
     * Triggers bulk salary disbursement via UPI.
     */
    @PostMapping("/batch/{id}/process")
    public ResponseEntity<ApiResponse<PayrollBatchResponse>> processBatch(
            @PathVariable("id") String batchId) {
        log.info("Received request to process payroll batch: {}", batchId);
        PayrollBatchResponse response = payrollService.processBatch(batchId);
        String message = response.getBatch().getStatus() == com.upimesh.payroll.model.enums.PayrollStatus.COMPLETED
                ? "Payroll batch fully completed successfully"
                : "Payroll batch processed with status: " + response.getBatch().getStatus();
        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    /**
     * GET /payroll/batch/{id}
     * Retrieves the details of a payroll batch.
     */
    @GetMapping("/batch/{id}")
    public ResponseEntity<ApiResponse<PayrollBatchResponse>> getBatchDetails(
            @PathVariable("id") String batchId) {
        log.info("Retrieving details for payroll batch: {}", batchId);
        PayrollBatchResponse details = payrollService.getBatchDetails(batchId);
        return ResponseEntity.ok(ApiResponse.success(details, "Payroll batch details retrieved successfully"));
    }

    /**
     * GET /payroll/slip/{paymentId}/pdf
     * Compiles and downloads an employee's salary slip as an A4 PDF.
     */
    @GetMapping("/slip/{paymentId}/pdf")
    public ResponseEntity<byte[]> downloadSalarySlip(
            @PathVariable String paymentId) {
        log.info("Request to download salary slip PDF for payment ID: {}", paymentId);
        byte[] pdfBytes = payrollService.generateSalarySlip(paymentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "payslip-" + paymentId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
