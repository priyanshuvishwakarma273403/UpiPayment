package com.upimesh.settlement.service;

import com.upimesh.settlement.feign.BankGatewayClient;
import com.upimesh.settlement.feign.BankGatewayClient.MerchantBankDetailsDto;
import com.upimesh.settlement.feign.TransactionServiceClient;
import com.upimesh.settlement.feign.TransactionServiceClient.UnsettledTransactionDto;
import com.upimesh.settlement.model.entity.MerchantSettlement;
import com.upimesh.settlement.model.entity.SettlementBatch;
import com.upimesh.settlement.model.entity.SettlementTransaction;
import com.upimesh.settlement.model.enums.SettlementMode;
import com.upimesh.settlement.model.enums.SettlementStatus;
import com.upimesh.settlement.model.response.SettlementBatchResponse;
import com.upimesh.settlement.repository.MerchantSettlementRepository;
import com.upimesh.settlement.repository.SettlementBatchRepository;
import com.upimesh.settlement.repository.SettlementTransactionRepository;
import com.upimesh.settlement.util.SettlementEncryptionUtil;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SettlementServiceTest {

    @Mock
    private SettlementBatchRepository batchRepo;

    @Mock
    private MerchantSettlementRepository merchantSettlementRepo;

    @Mock
    private SettlementTransactionRepository settlementTxnRepo;

    @Mock
    private TransactionServiceClient transactionServiceClient;

    @Mock
    private BankGatewayClient bankGatewayClient;

    @Mock
    private SettlementCalculationService calculationService;

    @Mock
    private BankTransferService bankTransferService;

    @InjectMocks
    private SettlementService settlementService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(settlementService, "serviceKey", "testKey");
        ReflectionTestUtils.setField(settlementService, "minSettleAmount", BigDecimal.valueOf(1.00));
        ReflectionTestUtils.setField(settlementService, "maxRetryCount", 3);
    }

    @Test
    public void runSettlementCreatesCorrectBatch() {
        LocalDate today = LocalDate.now();
        UnsettledTransactionDto txn = new UnsettledTransactionDto(
                "TX1", "merchant@upi", BigDecimal.valueOf(1000), "sender@upi", LocalDateTime.now(), "PAYMENT");

        when(batchRepo.findBySettlementDate(today)).thenReturn(Optional.empty());
        when(batchRepo.save(any(SettlementBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        when(transactionServiceClient.getUnsettledTransactions(eq("testKey"), any(), any()))
                .thenReturn(Collections.singletonList(txn));
        
        when(calculationService.groupTransactionsByMerchant(any()))
                .thenReturn(Collections.singletonMap("merchant@upi", Collections.singletonList(txn)));

        MerchantBankDetailsDto bankDetails = new MerchantBankDetailsDto(
                "ACC1", SettlementEncryptionUtil.encrypt("1234567890"), "XXXX5678", "HDFC0001234", "HDFC Bank", "TOK1");
        
        when(bankGatewayClient.getMerchantBankDetails("testKey", "merchant@upi")).thenReturn(bankDetails);
        when(calculationService.calculatePlatformFee(BigDecimal.valueOf(1000))).thenReturn(BigDecimal.valueOf(2.00));
        when(calculationService.calculateGst(BigDecimal.valueOf(2.00))).thenReturn(BigDecimal.valueOf(0.36));
        when(calculationService.calculateNetAmount(BigDecimal.valueOf(1000), BigDecimal.valueOf(2.00), BigDecimal.valueOf(0.36)))
                .thenReturn(BigDecimal.valueOf(997.64));
        when(calculationService.determineSettlementMode(BigDecimal.valueOf(997.64))).thenReturn(SettlementMode.IMPS);
        
        when(merchantSettlementRepo.save(any(MerchantSettlement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bankTransferService.initiateTransfer(any(), eq("1234567890"))).thenReturn("BNK1234567");

        SettlementBatchResponse response = settlementService.runDailySettlement();

        assertNotNull(response);
        assertEquals(SettlementStatus.COMPLETED, response.getStatus());
        assertEquals(1, response.getTotalMerchants());
        assertEquals(1, response.getTotalTransactions());
        assertEquals(BigDecimal.valueOf(1000), response.getTotalAmount());
        assertEquals(BigDecimal.valueOf(997.64), response.getProcessedAmount());
        
        verify(transactionServiceClient, times(1)).markTransactionsSettled(eq("testKey"), any(), any());
    }

    @Test
    public void skipsDuplicateBatch() {
        LocalDate today = LocalDate.now();
        SettlementBatch existing = SettlementBatch.builder()
                .batchId("BAT_EXISTING")
                .settlementDate(today)
                .status(SettlementStatus.COMPLETED)
                .build();

        when(batchRepo.findBySettlementDate(today)).thenReturn(Optional.of(existing));

        SettlementBatchResponse response = settlementService.runDailySettlement();

        assertNotNull(response);
        assertEquals("BAT_EXISTING", response.getBatchId());
        assertEquals(SettlementStatus.COMPLETED, response.getStatus());
        verifyNoInteractions(transactionServiceClient, bankGatewayClient);
    }

    @Test
    public void handlesPartialMerchantFailure() {
        LocalDate today = LocalDate.now();
        UnsettledTransactionDto txn = new UnsettledTransactionDto(
                "TX1", "merchant@upi", BigDecimal.valueOf(1000), "sender@upi", LocalDateTime.now(), "PAYMENT");

        when(batchRepo.findBySettlementDate(today)).thenReturn(Optional.empty());
        when(batchRepo.save(any(SettlementBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        when(transactionServiceClient.getUnsettledTransactions(eq("testKey"), any(), any()))
                .thenReturn(Collections.singletonList(txn));
        
        when(calculationService.groupTransactionsByMerchant(any()))
                .thenReturn(Collections.singletonMap("merchant@upi", Collections.singletonList(txn)));

        when(bankGatewayClient.getMerchantBankDetails("testKey", "merchant@upi")).thenReturn(null); // bank details missing
        
        when(merchantSettlementRepo.save(any(MerchantSettlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SettlementBatchResponse response = settlementService.runDailySettlement();

        assertNotNull(response);
        assertEquals(SettlementStatus.PARTIALLY_FAILED, response.getStatus());
        assertEquals(BigDecimal.valueOf(1000), response.getFailedAmount());
        assertEquals(BigDecimal.ZERO, response.getProcessedAmount());
    }

    @Test
    public void retryFailedSettlement() {
        MerchantSettlement failedMs = MerchantSettlement.builder()
                .settlementId("SET123")
                .batchId("BAT123")
                .merchantUpiId("merchant@upi")
                .grossAmount(BigDecimal.valueOf(1000))
                .netAmount(BigDecimal.valueOf(997.64))
                .mode(SettlementMode.IMPS)
                .status(SettlementStatus.FAILED)
                .retryCount(0)
                .build();

        when(merchantSettlementRepo.findByStatusAndRetryCountLessThan(SettlementStatus.FAILED, 3))
                .thenReturn(Collections.singletonList(failedMs));

        MerchantBankDetailsDto bankDetails = new MerchantBankDetailsDto(
                "ACC1", SettlementEncryptionUtil.encrypt("1234567890"), "XXXX5678", "HDFC0001234", "HDFC Bank", "TOK1");
        
        when(bankGatewayClient.getMerchantBankDetails("testKey", "merchant@upi")).thenReturn(bankDetails);
        when(bankTransferService.initiateTransfer(any(), eq("1234567890"))).thenReturn("BNK7777");

        SettlementBatch parentBatch = SettlementBatch.builder()
                .batchId("BAT123")
                .processedAmount(BigDecimal.ZERO)
                .failedAmount(BigDecimal.valueOf(1000))
                .status(SettlementStatus.PARTIALLY_FAILED)
                .build();

        when(batchRepo.findByBatchId("BAT123")).thenReturn(Optional.of(parentBatch));

        settlementService.retryFailedSettlements();

        assertEquals(SettlementStatus.COMPLETED, failedMs.getStatus());
        assertEquals("BNK7777", failedMs.getBankReferenceNumber());
        assertEquals(1, failedMs.getRetryCount());
        assertEquals(SettlementStatus.COMPLETED, parentBatch.getStatus());
        assertEquals(BigDecimal.valueOf(997.64), parentBatch.getProcessedAmount());
        assertEquals(BigDecimal.ZERO, parentBatch.getFailedAmount());
    }
}
