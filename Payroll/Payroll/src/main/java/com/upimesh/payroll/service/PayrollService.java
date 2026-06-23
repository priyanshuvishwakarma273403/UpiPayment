package com.upimesh.payroll.service;

import com.upimesh.payroll.exception.EmployeePaymentNotFoundException;
import com.upimesh.payroll.exception.PayrollBatchNotFoundException;
import com.upimesh.payroll.feign.BankGatewayServiceClient;
import com.upimesh.payroll.feign.NpciServiceClient;
import com.upimesh.payroll.feign.dto.BankApiResponse;
import com.upimesh.payroll.feign.dto.InitiateTransactionRequest;
import com.upimesh.payroll.feign.dto.NpciApiResponse;
import com.upimesh.payroll.feign.dto.TransactionResponse;
import com.upimesh.payroll.feign.dto.TransactionStatus;
import com.upimesh.payroll.feign.dto.TransactionType;
import com.upimesh.payroll.feign.dto.UpiResolveResponse;
import com.upimesh.payroll.model.entity.EmployeePayment;
import com.upimesh.payroll.model.entity.PayrollBatch;
import com.upimesh.payroll.model.enums.EmployeePaymentStatus;
import com.upimesh.payroll.model.enums.PayrollStatus;
import com.upimesh.payroll.model.request.EmployeeSalaryInput;
import com.upimesh.payroll.model.response.DeductionResult;
import com.upimesh.payroll.model.response.PayrollBatchResponse;
import com.upimesh.payroll.repository.EmployeePaymentRepository;
import com.upimesh.payroll.repository.PayrollBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {

    private final PayrollBatchRepository batchRepository;
    private final EmployeePaymentRepository paymentRepository;
    private final SalaryCalculationService salaryCalculationService;
    private final PdfGenerationService pdfGenerationService;
    private final BankGatewayServiceClient bankGatewayServiceClient;
    private final NpciServiceClient npciServiceClient;

    @Value("${payroll.internal-key:internal-secret-change-in-prod}")
    private String internalKey;

    /**
     * Creates a new payroll batch, calculates deductions for each employee, and registers PENDING employee payments.
     */
    @Transactional
    public PayrollBatchResponse createPayrollBatch(String companyId, String companyUpiId, List<EmployeeSalaryInput> employees, String month) {
        log.info("Creating payroll batch for company: {} | month: {} | employeeCount: {}", companyId, month, employees.size());

        String batchId = "BAT" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 15).toUpperCase();

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        List<EmployeePayment> payments = new ArrayList<>();

        for (EmployeeSalaryInput emp : employees) {
            // Validate UPI ID format
            if (!emp.getEmployeeUpiId().matches("^[a-zA-Z0-9._-]+@[a-zA-Z]{2,}$")) {
                throw new IllegalArgumentException("Invalid UPI handle format for employee: " + emp.getEmployeeName() + " (" + emp.getEmployeeUpiId() + ")");
            }

            // Calculate TDS, PF, ESI
            DeductionResult dec = salaryCalculationService.calculateDeductions(emp.getGrossSalary());

            // Mask bank account (keep last 4 digits)
            String rawAcc = emp.getBankAccount();
            String maskedAcc = rawAcc.length() > 4 
                    ? "XXXXXX" + rawAcc.substring(rawAcc.length() - 4) 
                    : rawAcc;

            String paymentId = "PAY" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 15).toUpperCase();

            EmployeePayment payment = EmployeePayment.builder()
                    .paymentId(paymentId)
                    .batchId(batchId)
                    .employeeId(emp.getEmployeeId())
                    .employeeName(emp.getEmployeeName())
                    .employeeUpiId(emp.getEmployeeUpiId())
                    .bankAccount(maskedAcc)
                    .grossSalary(emp.getGrossSalary())
                    .pfDeduction(dec.pf())
                    .esiDeduction(dec.esi())
                    .tdsDeduction(dec.tds())
                    .otherDeductions(BigDecimal.ZERO)
                    .netSalary(dec.netSalary())
                    .status(EmployeePaymentStatus.PENDING)
                    .build();

            payments.add(payment);

            totalGross = totalGross.add(emp.getGrossSalary());
            totalDeductions = totalDeductions.add(dec.totalDeductions());
            totalNet = totalNet.add(dec.netSalary());
        }

        // Save payments
        payments = paymentRepository.saveAll(payments);

        // Save Batch
        PayrollBatch batch = PayrollBatch.builder()
                .batchId(batchId)
                .companyId(companyId)
                .companyUpiId(companyUpiId)
                .month(month)
                .employeeCount(employees.size())
                .totalGrossAmount(totalGross)
                .totalDeductions(totalDeductions)
                .totalNetAmount(totalNet)
                .status(PayrollStatus.DRAFT)
                .build();

        batch = batchRepository.save(batch);

        log.info("Payroll batch {} created as DRAFT. Total Net: ₹{}", batchId, totalNet);

        return new PayrollBatchResponse(batch, payments);
    }

    /**
     * Validates employee bank accounts via the bank-gateway-service.
     */
    @Transactional
    public PayrollBatch validateBatch(String batchId) {
        log.info("Validating bank accounts for payroll batch: {}", batchId);

        PayrollBatch batch = batchRepository.findByBatchId(batchId)
                .orElseThrow(() -> new PayrollBatchNotFoundException("Payroll batch not found: " + batchId));

        if (batch.getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException("Payroll batch must be in DRAFT status to validate. Current: " + batch.getStatus());
        }

        List<EmployeePayment> payments = paymentRepository.findByBatchId(batchId);

        for (EmployeePayment payment : payments) {
            try {
                // Call bank gateway feign client to resolve employee UPI ID and check account status
                BankApiResponse<UpiResolveResponse> apiResponse = bankGatewayServiceClient.resolveUpiHandle(internalKey, payment.getEmployeeUpiId());
                
                if (apiResponse == null || !apiResponse.isSuccess() || apiResponse.getData() == null) {
                    String msg = apiResponse != null ? apiResponse.getMessage() : "Null response from bank gateway";
                    throw new IllegalArgumentException("Validation failed for employee " + payment.getEmployeeName() + ": " + msg);
                }

                UpiResolveResponse resolve = apiResponse.getData();
                if (!resolve.isActive()) {
                    throw new IllegalArgumentException("Validation failed: Employee account " + payment.getEmployeeName() + " is inactive.");
                }

            } catch (Exception e) {
                log.error("Account verification failed for employee: {}", payment.getEmployeeName(), e);
                throw new IllegalArgumentException("Verification failed for employee " + payment.getEmployeeName() + ": " + e.getMessage(), e);
            }
        }

        batch.setStatus(PayrollStatus.VALIDATED);
        log.info("Payroll batch {} validated successfully.", batchId);
        return batchRepository.save(batch);
    }

    /**
     * Processes bulk payroll salary disbursement via NPCI transaction client.
     */
    @Transactional
    public PayrollBatchResponse processBatch(String batchId) {
        log.info("Processing payroll batch: {}", batchId);

        PayrollBatch batch = batchRepository.findByBatchId(batchId)
                .orElseThrow(() -> new PayrollBatchNotFoundException("Payroll batch not found: " + batchId));

        if (batch.getStatus() != PayrollStatus.VALIDATED) {
            throw new IllegalStateException("Payroll batch must be in VALIDATED status to process. Current: " + batch.getStatus());
        }

        // Move status to PROCESSING immediately to prevent concurrent triggers
        batch.setStatus(PayrollStatus.PROCESSING);
        batchRepository.saveAndFlush(batch);

        List<EmployeePayment> payments = paymentRepository.findByBatchId(batchId);

        int processed = 0;
        int failed = 0;

        for (EmployeePayment payment : payments) {
            if (payment.getStatus() == EmployeePaymentStatus.SUCCESS) {
                processed++;
                continue;
            }

            InitiateTransactionRequest request = InitiateTransactionRequest.builder()
                    .senderUpiId(batch.getCompanyUpiId())
                    .receiverUpiId(payment.getEmployeeUpiId())
                    .amount(payment.getNetSalary())
                    .remarks("Salary " + batch.getMonth())
                    .type(TransactionType.P2P)
                    .deviceId("PayrollServiceServer")
                    .ipAddress("127.0.0.1")
                    .mpinHash("company-mpin-hash")
                    .idempotencyKey("PAY-" + payment.getPaymentId())
                    .build();

            try {
                NpciApiResponse<TransactionResponse> apiResponse = npciServiceClient.initiateTransaction(internalKey, request);
                
                if (apiResponse != null && apiResponse.isSuccess() && apiResponse.getData() != null 
                        && apiResponse.getData().getStatus() == TransactionStatus.SUCCESS) {
                    
                    payment.setStatus(EmployeePaymentStatus.SUCCESS);
                    payment.setTransactionId(apiResponse.getData().getTransactionId());
                    payment.setProcessedAt(LocalDateTime.now());
                    processed++;
                } else {
                    String reason = apiResponse != null ? apiResponse.getMessage() : "NPCI transaction was not successful";
                    throw new RuntimeException(reason);
                }

            } catch (Exception e) {
                log.error("Salary transfer failed for employee: {}", payment.getEmployeeName(), e);
                payment.setStatus(EmployeePaymentStatus.FAILED);
                payment.setFailureReason(e.getMessage());
                payment.setProcessedAt(LocalDateTime.now());
                failed++;
            }

            paymentRepository.save(payment);
        }

        // Determine final batch status
        if (failed == 0) {
            batch.setStatus(PayrollStatus.COMPLETED);
        } else if (processed == 0) {
            batch.setStatus(PayrollStatus.FAILED);
        } else {
            batch.setStatus(PayrollStatus.PARTIALLY_FAILED);
        }

        batch.setProcessedCount(processed);
        batch.setFailedCount(failed);
        batch.setProcessedAt(LocalDateTime.now());

        batch = batchRepository.save(batch);
        log.info("Batch processing complete: {} | Success: {} | Failed: {}", batchId, processed, failed);

        return new PayrollBatchResponse(batch, payments);
    }

    /**
     * Generates a payslip PDF for an individual employee payment.
     */
    public byte[] generateSalarySlip(String paymentId) {
        log.info("Compiling payslip PDF for payment ID: {}", paymentId);

        EmployeePayment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new EmployeePaymentNotFoundException("Employee payment details not found: " + paymentId));

        PayrollBatch batch = batchRepository.findByBatchId(payment.getBatchId())
                .orElseThrow(() -> new PayrollBatchNotFoundException("Payroll batch details not found for payment: " + paymentId));

        return pdfGenerationService.generateSalarySlipPdf(payment, batch);
    }

    public PayrollBatchResponse getBatchDetails(String batchId) {
        PayrollBatch batch = batchRepository.findByBatchId(batchId)
                .orElseThrow(() -> new PayrollBatchNotFoundException("Payroll batch not found: " + batchId));

        List<EmployeePayment> payments = paymentRepository.findByBatchId(batchId);
        return new PayrollBatchResponse(batch, payments);
    }

    public List<PayrollBatch> getCompanyPayrollHistory(String companyId) {
        return batchRepository.findByCompanyId(companyId);
    }
}
