package com.upimesh.dispute.service;

import com.upimesh.dispute.exception.DisputeNotFoundException;
import com.upimesh.dispute.feign.NotificationServiceClient;
import com.upimesh.dispute.feign.NpciServiceClient;
import com.upimesh.dispute.feign.dto.*;
import com.upimesh.dispute.model.entity.Dispute;
import com.upimesh.dispute.model.entity.DisputeEvidence;
import com.upimesh.dispute.model.enums.DisputeReason;
import com.upimesh.dispute.model.enums.DisputeStatus;
import com.upimesh.dispute.model.request.MerchantResponseRequest;
import com.upimesh.dispute.model.request.RaiseDisputeRequest;
import com.upimesh.dispute.model.request.ResolveDisputeRequest;
import com.upimesh.dispute.model.response.DisputeResponse;
import com.upimesh.dispute.repository.DisputeEvidenceRepository;
import com.upimesh.dispute.repository.DisputeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DisputeServiceTest {

    @Mock private DisputeRepository disputeRepository;
    @Mock private DisputeEvidenceRepository evidenceRepository;
    @Mock private NpciServiceClient npciServiceClient;
    @Mock private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private DisputeService disputeService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(disputeService, "internalKey", "test-internal-key");
    }

    @Test
    void testRaiseDispute_Success() {
        RaiseDisputeRequest request = RaiseDisputeRequest.builder()
                .userId("user123")
                .userUpiId("user@pay")
                .transactionId("TXN001")
                .reason(DisputeReason.UNAUTHORIZED)
                .description("I did not authorize this charge")
                .build();

        TransactionResponse txn = TransactionResponse.builder()
                .transactionId("TXN001")
                .senderUpiId("user@pay")
                .receiverUpiId("merchant@pay")
                .amount(new BigDecimal("1000.00"))
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        NpciApiResponse<TransactionResponse> apiResponse = NpciApiResponse.<TransactionResponse>builder()
                .success(true)
                .data(txn)
                .build();

        when(disputeRepository.findByTransactionId("TXN001")).thenReturn(Optional.empty());
        when(npciServiceClient.checkStatus(anyString(), eq("TXN001"))).thenReturn(apiResponse);
        when(disputeRepository.save(any(Dispute.class))).thenAnswer(inv -> {
            Dispute saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        DisputeResponse response = disputeService.raiseDispute(request);

        assertNotNull(response);
        assertEquals("user@pay", response.getUserUpiId());
        assertEquals("merchant@pay", response.getMerchantUpiId());
        assertEquals(DisputeStatus.MERCHANT_NOTIFIED, response.getStatus());
        assertEquals(new BigDecimal("1000.00"), response.getAmount());

        verify(disputeRepository, times(1)).save(any(Dispute.class));
        verify(notificationServiceClient, times(1)).sendPaymentNotification(eq("test-internal-key"), any());
    }

    @Test
    void testRaiseDispute_TooOldTransaction() {
        RaiseDisputeRequest request = RaiseDisputeRequest.builder()
                .userId("user123")
                .userUpiId("user@pay")
                .transactionId("TXN001")
                .reason(DisputeReason.UNAUTHORIZED)
                .description("I did not authorize this charge")
                .build();

        TransactionResponse txn = TransactionResponse.builder()
                .transactionId("TXN001")
                .senderUpiId("user@pay")
                .receiverUpiId("merchant@pay")
                .amount(new BigDecimal("1000.00"))
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now().minusDays(95)) // Exceeds 90 days limit
                .build();

        NpciApiResponse<TransactionResponse> apiResponse = NpciApiResponse.<TransactionResponse>builder()
                .success(true)
                .data(txn)
                .build();

        when(disputeRepository.findByTransactionId("TXN001")).thenReturn(Optional.empty());
        when(npciServiceClient.checkStatus(anyString(), eq("TXN001"))).thenReturn(apiResponse);

        assertThrows(IllegalArgumentException.class, () -> disputeService.raiseDispute(request));
        verify(disputeRepository, never()).save(any());
    }

    @Test
    void testSubmitMerchantResponse() {
        MerchantResponseRequest request = MerchantResponseRequest.builder()
                .merchantUpiId("merchant@pay")
                .response("Services were fully delivered")
                .evidenceUrls(Collections.singletonList("http://receipt-url.com"))
                .build();

        Dispute dispute = Dispute.builder()
                .disputeId("DSP001")
                .merchantUpiId("merchant@pay")
                .status(DisputeStatus.MERCHANT_NOTIFIED)
                .evidenceUrls(new ArrayList<>())
                .build();

        when(disputeRepository.findByDisputeId("DSP001")).thenReturn(Optional.of(dispute));
        when(disputeRepository.save(any(Dispute.class))).thenAnswer(inv -> inv.getArgument(0));

        DisputeResponse response = disputeService.submitMerchantResponse("DSP001", request);

        assertEquals(DisputeStatus.UNDER_REVIEW, response.getStatus());
        assertEquals("Services were fully delivered", response.getMerchantResponse());
        assertTrue(response.getEvidenceUrls().contains("http://receipt-url.com"));
        verify(evidenceRepository, times(1)).save(any(DisputeEvidence.class));
        verify(disputeRepository, times(1)).save(dispute);
    }

    @Test
    void testResolveDispute_InFavorOfUser() {
        ResolveDisputeRequest request = ResolveDisputeRequest.builder()
                .inFavorOf("USER")
                .notes("Fraud proven")
                .resolvedBy("admin-01")
                .build();

        Dispute dispute = Dispute.builder()
                .disputeId("DSP001")
                .transactionId("TXN001")
                .amount(new BigDecimal("1000.00"))
                .status(DisputeStatus.UNDER_REVIEW)
                .build();

        RefundResponse refundResponse = RefundResponse.builder()
                .refundId("RFD001")
                .status(RefundStatus.COMPLETED)
                .build();

        NpciApiResponse<RefundResponse> apiResponse = NpciApiResponse.<RefundResponse>builder()
                .success(true)
                .data(refundResponse)
                .build();

        when(disputeRepository.findByDisputeId("DSP001")).thenReturn(Optional.of(dispute));
        when(npciServiceClient.initiateRefund(anyString(), any(RefundRequest.class))).thenReturn(apiResponse);
        when(disputeRepository.save(any(Dispute.class))).thenAnswer(inv -> inv.getArgument(0));

        DisputeResponse response = disputeService.resolveDispute("DSP001", request);

        assertEquals(DisputeStatus.RESOLVED_REFUND, response.getStatus());
        assertEquals("USER", response.getResolvedInFavorOf());
        assertTrue(response.getResolutionNotes().contains("Fraud proven"));
        verify(disputeRepository, times(1)).save(dispute);
        verify(notificationServiceClient, times(1)).sendPaymentNotification(eq("test-internal-key"), any());
    }

    @Test
    void testResolveDispute_InFavorOfMerchant() {
        ResolveDisputeRequest request = ResolveDisputeRequest.builder()
                .inFavorOf("MERCHANT")
                .notes("Evidence shows user received services")
                .resolvedBy("admin-01")
                .build();

        Dispute dispute = Dispute.builder()
                .disputeId("DSP001")
                .transactionId("TXN001")
                .status(DisputeStatus.UNDER_REVIEW)
                .build();

        when(disputeRepository.findByDisputeId("DSP001")).thenReturn(Optional.of(dispute));
        when(disputeRepository.save(any(Dispute.class))).thenAnswer(inv -> inv.getArgument(0));

        DisputeResponse response = disputeService.resolveDispute("DSP001", request);

        assertEquals(DisputeStatus.RESOLVED_DISMISSED, response.getStatus());
        assertEquals("MERCHANT", response.getResolvedInFavorOf());
        verify(npciServiceClient, never()).initiateRefund(anyString(), any());
        verify(disputeRepository, times(1)).save(dispute);
    }

    @Test
    void testCheckExpiredMerchantResponses_AutoResolves() {
        Dispute dispute = Dispute.builder()
                .disputeId("DSP001")
                .transactionId("TXN001")
                .amount(new BigDecimal("1000.00"))
                .status(DisputeStatus.MERCHANT_NOTIFIED)
                .merchantResponseDeadline(LocalDateTime.now().minusHours(1))
                .build();

        RefundResponse refundResponse = RefundResponse.builder()
                .status(RefundStatus.COMPLETED)
                .build();

        NpciApiResponse<RefundResponse> apiResponse = NpciApiResponse.<RefundResponse>builder()
                .success(true)
                .data(refundResponse)
                .build();

        when(disputeRepository.findByStatusAndMerchantResponseDeadlineLessThanEqual(eq(DisputeStatus.MERCHANT_NOTIFIED), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(dispute));
        when(npciServiceClient.initiateRefund(anyString(), any(RefundRequest.class))).thenReturn(apiResponse);

        disputeService.checkExpiredMerchantResponses();

        assertEquals(DisputeStatus.RESOLVED_REFUND, dispute.getStatus());
        assertEquals("USER", dispute.getResolvedInFavorOf());
        verify(disputeRepository, times(1)).save(dispute);
    }
}
