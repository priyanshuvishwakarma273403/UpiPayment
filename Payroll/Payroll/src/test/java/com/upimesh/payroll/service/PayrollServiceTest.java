package com.upimesh.payroll.service;

import com.upimesh.payroll.feign.BankGatewayServiceClient;
import com.upimesh.payroll.feign.NpciServiceClient;
import com.upimesh.payroll.feign.dto.BankApiResponse;
import com.upimesh.payroll.feign.dto.InitiateTransactionRequest;
import com.upimesh.payroll.feign.dto.NpciApiResponse;
import com.upimesh.payroll.feign.dto.TransactionResponse;
import com.upimesh.payroll.feign.dto.TransactionStatus;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PayrollServiceTest {

    @Mock
    private PayrollBatchRepository batchRepository;

    @Mock
    private EmployeePaymentRepository paymentRepository;

    @Mock
    private BankGatewayServiceClient bankGatewayServiceClient;

    @Mock
    private NpciServiceClient npciServiceClient;

    @Spy
    private SalaryCalculationService salaryCalculationService;

    @Mock
    private PdfGenerationService pdfGenerationService;

    @InjectMocks
    private PayrollService payrollService;

    private String companyId;
    private String companyUpiId;

    @BeforeEach
    public void setUp() {
        companyId = "COMP999";
        companyUpiId = "company@hdfc";
        org.springframework.test.util.ReflectionTestUtils.setField(payrollService, "internalKey", "test-secret-key");
    }

    // ─── 1. SALARY DEDUCTION CALCULATION TESTS ───────────────────────────────

    @Test
    public void testDeductionCalculations_PF_Cap_And_ESI_Threshold() {
        SalaryCalculationService service = new SalaryCalculationService();

        // Case A: Gross salary ₹100,000 (basic is ₹50,000, capped at ₹15,000 for PF)
        // ESI is not applicable since gross > ₹21,000
        // TDS Slab: Annual Gross ₹12,00,000.
        // TDS Slab math: 0-2.5L: 0; 2.5-5L: 12.5k; 5-10L: 100k; 10-12L: 2L * 30% = 60k. Total annual = 172.5k. Monthly TDS = 14.375k = 14375.
        // PF = 15000 * 12% = 1800. Net = 100k - (1800 + 14375) = 83825.
        DeductionResult resHigh = service.calculateDeductions(BigDecimal.valueOf(100000.00));
        assertEquals(BigDecimal.valueOf(1800.00).setScale(2), resHigh.pf());
        assertEquals(BigDecimal.ZERO.setScale(2), resHigh.esi());
        assertEquals(BigDecimal.valueOf(14375.00).setScale(2), resHigh.tds());
        assertEquals(BigDecimal.valueOf(16175.00).setScale(2), resHigh.totalDeductions());
        assertEquals(BigDecimal.valueOf(83825.00).setScale(2), resHigh.netSalary());

        // Case B: Gross salary ₹20,000 (basic is ₹10,000, under cap)
        // ESI is applicable: ₹20,000 * 0.75% = ₹150
        // TDS: Annual Gross ₹2,40,000 (under ₹2.5L, so TDS = 0)
        // PF = 10,000 * 12% = 1,200. Total deductions = 1,350. Net = 18,650.
        DeductionResult resLow = service.calculateDeductions(BigDecimal.valueOf(20000.00));
        assertEquals(BigDecimal.valueOf(1200.00).setScale(2), resLow.pf());
        assertEquals(BigDecimal.valueOf(150.00).setScale(2), resLow.esi());
        assertEquals(BigDecimal.ZERO.setScale(2), resLow.tds());
        assertEquals(BigDecimal.valueOf(1350.00).setScale(2), resLow.totalDeductions());
        assertEquals(BigDecimal.valueOf(18650.00).setScale(2), resLow.netSalary());
    }

    // ─── 2. BATCH VALIDATION TESTS ───────────────────────────────────────────

    @Test
    public void testValidateBatch_Success() {
        String batchId = "BAT111";
        PayrollBatch batch = PayrollBatch.builder()
                .batchId(batchId)
                .status(PayrollStatus.DRAFT)
                .build();

        EmployeePayment payment = EmployeePayment.builder()
                .employeeUpiId("emp1@upimesh")
                .employeeName("Emp One")
                .build();

        when(batchRepository.findByBatchId(batchId)).thenReturn(Optional.of(batch));
        when(paymentRepository.findByBatchId(batchId)).thenReturn(Collections.singletonList(payment));
        when(batchRepository.save(any(PayrollBatch.class))).thenAnswer(i -> i.getArgument(0));

        UpiResolveResponse resolve = UpiResolveResponse.builder()
                .upiHandle("emp1@upimesh")
                .isActive(true)
                .build();
        BankApiResponse<UpiResolveResponse> apiResponse = BankApiResponse.<UpiResolveResponse>builder()
                .success(true)
                .data(resolve)
                .build();

        when(bankGatewayServiceClient.resolveUpiHandle(eq("test-secret-key"), eq("emp1@upimesh")))
                .thenReturn(apiResponse);

        PayrollBatch result = payrollService.validateBatch(batchId);

        assertNotNull(result);
        assertEquals(PayrollStatus.VALIDATED, result.getStatus());
    }

    // ─── 3. BATCH PROCESSING TESTS (HAPPY PATH) ──────────────────────────────

    @Test
    public void testProcessBatch_FullSuccess() {
        String batchId = "BAT111";
        PayrollBatch batch = PayrollBatch.builder()
                .batchId(batchId)
                .companyUpiId(companyUpiId)
                .month("2024-01")
                .employeeCount(2)
                .status(PayrollStatus.VALIDATED)
                .build();

        EmployeePayment payment1 = EmployeePayment.builder()
                .paymentId("PAY1")
                .employeeUpiId("emp1@upimesh")
                .netSalary(BigDecimal.valueOf(15000.00))
                .status(EmployeePaymentStatus.PENDING)
                .build();

        EmployeePayment payment2 = EmployeePayment.builder()
                .paymentId("PAY2")
                .employeeUpiId("emp2@upimesh")
                .netSalary(BigDecimal.valueOf(18000.00))
                .status(EmployeePaymentStatus.PENDING)
                .build();

        when(batchRepository.findByBatchId(batchId)).thenReturn(Optional.of(batch));
        when(paymentRepository.findByBatchId(batchId)).thenReturn(Arrays.asList(payment1, payment2));
        when(batchRepository.save(any(PayrollBatch.class))).thenAnswer(i -> i.getArgument(0));

        TransactionResponse txnSuccess = TransactionResponse.builder()
                .transactionId("TXN555")
                .status(TransactionStatus.SUCCESS)
                .build();
        NpciApiResponse<TransactionResponse> npciResponse = NpciApiResponse.<TransactionResponse>builder()
                .success(true)
                .data(txnSuccess)
                .build();

        when(npciServiceClient.initiateTransaction(eq("test-secret-key"), any(InitiateTransactionRequest.class)))
                .thenReturn(npciResponse);

        PayrollBatchResponse response = payrollService.processBatch(batchId);

        assertNotNull(response);
        assertEquals(PayrollStatus.COMPLETED, response.getBatch().getStatus());
        assertEquals(2, response.getBatch().getProcessedCount());
        assertEquals(0, response.getBatch().getFailedCount());
        assertEquals(EmployeePaymentStatus.SUCCESS, payment1.getStatus());
        assertEquals(EmployeePaymentStatus.SUCCESS, payment2.getStatus());
    }

    // ─── 4. BATCH PROCESSING TESTS (PARTIAL FAILURE HANDLING) ───────────────

    @Test
    public void testProcessBatch_PartialFailure() {
        String batchId = "BAT111";
        PayrollBatch batch = PayrollBatch.builder()
                .batchId(batchId)
                .companyUpiId(companyUpiId)
                .month("2024-01")
                .employeeCount(2)
                .status(PayrollStatus.VALIDATED)
                .build();

        EmployeePayment payment1 = EmployeePayment.builder()
                .paymentId("PAY1")
                .employeeUpiId("emp1@upimesh")
                .netSalary(BigDecimal.valueOf(15000.00))
                .status(EmployeePaymentStatus.PENDING)
                .build();

        EmployeePayment payment2 = EmployeePayment.builder()
                .paymentId("PAY2")
                .employeeUpiId("emp2@upimesh")
                .netSalary(BigDecimal.valueOf(18000.00))
                .status(EmployeePaymentStatus.PENDING)
                .build();

        when(batchRepository.findByBatchId(batchId)).thenReturn(Optional.of(batch));
        when(paymentRepository.findByBatchId(batchId)).thenReturn(Arrays.asList(payment1, payment2));
        when(batchRepository.save(any(PayrollBatch.class))).thenAnswer(i -> i.getArgument(0));

        // Emp 1 Payout succeeds
        TransactionResponse txnSuccess = TransactionResponse.builder()
                .transactionId("TXN555")
                .status(TransactionStatus.SUCCESS)
                .build();
        NpciApiResponse<TransactionResponse> npciSuccess = NpciApiResponse.<TransactionResponse>builder()
                .success(true)
                .data(txnSuccess)
                .build();

        // Emp 2 Payout fails
        NpciApiResponse<TransactionResponse> npciFail = NpciApiResponse.<TransactionResponse>builder()
                .success(false)
                .message("NPCI Timeout")
                .build();

        when(npciServiceClient.initiateTransaction(eq("test-secret-key"), any(InitiateTransactionRequest.class)))
                .thenReturn(npciSuccess)
                .thenReturn(npciFail);

        PayrollBatchResponse response = payrollService.processBatch(batchId);

        assertNotNull(response);
        assertEquals(PayrollStatus.PARTIALLY_FAILED, response.getBatch().getStatus());
        assertEquals(1, response.getBatch().getProcessedCount());
        assertEquals(1, response.getBatch().getFailedCount());
        assertEquals(EmployeePaymentStatus.SUCCESS, payment1.getStatus());
        assertEquals(EmployeePaymentStatus.FAILED, payment2.getStatus());
        assertEquals("NPCI Timeout", payment2.getFailureReason());
    }
}
