package com.upimesh.subscription.service;

import com.upimesh.subscription.feign.NpciServiceClient;
import com.upimesh.subscription.feign.dto.*;
import com.upimesh.subscription.model.entity.Subscription;
import com.upimesh.subscription.model.entity.SubscriptionPaymentAttempt;
import com.upimesh.subscription.model.enums.BillingCycle;
import com.upimesh.subscription.model.enums.PaymentAttemptStatus;
import com.upimesh.subscription.model.enums.SubscriptionStatus;
import com.upimesh.subscription.model.request.CreateSubscriptionRequest;
import com.upimesh.subscription.model.response.SubscriptionResponse;
import com.upimesh.subscription.repository.SubscriptionPaymentAttemptRepository;
import com.upimesh.subscription.repository.SubscriptionRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private SubscriptionPaymentAttemptRepository attemptRepository;
    @Mock private NpciServiceClient npciServiceClient;
    @Mock private DunningService dunningService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(subscriptionService, "internalKey", "test-internal-key");
    }

    @Test
    void testCreateSubscription_Success() {
        CreateSubscriptionRequest request = CreateSubscriptionRequest.builder()
                .userId("user123")
                .userUpiId("user@pay")
                .merchantUpiId("merchant@pay")
                .merchantName("Spotify Premium")
                .planName("Premium Family")
                .amount(new BigDecimal("179.00"))
                .billingCycle(BillingCycle.MONTHLY)
                .build();

        MandateResponse npciMandate = MandateResponse.builder()
                .mandateId("MND12345")
                .status(MandateStatus.ACTIVE)
                .build();

        NpciApiResponse<MandateResponse> apiResponse = NpciApiResponse.<MandateResponse>builder()
                .success(true)
                .data(npciMandate)
                .build();

        when(npciServiceClient.createMandate(anyString(), any(MandateCreateRequest.class)))
                .thenReturn(apiResponse);

        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        SubscriptionResponse response = subscriptionService.createSubscription(request);

        assertNotNull(response);
        assertEquals("user123", response.getUserId());
        assertEquals("MND12345", response.getMandateId());
        assertEquals(SubscriptionStatus.ACTIVE, response.getStatus());
        verify(npciServiceClient, times(1)).createMandate(eq("test-internal-key"), any(MandateCreateRequest.class));
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    void testExecuteScheduledPayments_Success() {
        Subscription subscription = Subscription.builder()
                .id(1L)
                .subscriptionId("SUB123")
                .userId("user123")
                .userUpiId("user@pay")
                .merchantUpiId("merchant@pay")
                .planName("Monthly Plan")
                .amount(new BigDecimal("99.00"))
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .nextBillingDate(LocalDate.now())
                .failedAttempts(0)
                .build();

        SubscriptionPaymentAttempt attempt = SubscriptionPaymentAttempt.builder()
                .attemptId("ATT123")
                .status(PaymentAttemptStatus.PENDING)
                .build();

        TransactionResponse txResponse = TransactionResponse.builder()
                .transactionId("TXN123456")
                .status(TransactionStatus.SUCCESS)
                .build();

        NpciApiResponse<TransactionResponse> apiResponse = NpciApiResponse.<TransactionResponse>builder()
                .success(true)
                .data(txResponse)
                .build();

        when(subscriptionRepository.findByStatusAndNextBillingDateLessThanEqual(eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(subscription));

        when(attemptRepository.save(any(SubscriptionPaymentAttempt.class))).thenReturn(attempt);

        when(npciServiceClient.initiateTransaction(anyString(), any(InitiateTransactionRequest.class)))
                .thenReturn(apiResponse);

        subscriptionService.executeScheduledPayments();

        assertEquals(0, subscription.getFailedAttempts());
        assertEquals(LocalDate.now().plusMonths(1), subscription.getNextBillingDate());
        verify(attemptRepository, times(2)).save(any(SubscriptionPaymentAttempt.class));
        verify(subscriptionRepository, times(1)).save(subscription);
    }

    @Test
    void testExecuteScheduledPayments_DunningStep1() {
        Subscription subscription = Subscription.builder()
                .id(1L)
                .subscriptionId("SUB123")
                .userId("user123")
                .userUpiId("user@pay")
                .merchantUpiId("merchant@pay")
                .planName("Monthly Plan")
                .amount(new BigDecimal("99.00"))
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .nextBillingDate(LocalDate.now())
                .failedAttempts(0)
                .maxRetries(3)
                .build();

        SubscriptionPaymentAttempt attempt = SubscriptionPaymentAttempt.builder()
                .attemptId("ATT123")
                .status(PaymentAttemptStatus.PENDING)
                .build();

        TransactionResponse txResponse = TransactionResponse.builder()
                .status(TransactionStatus.FAILED)
                .npciResponseMessage("Insufficient funds")
                .build();

        NpciApiResponse<TransactionResponse> apiResponse = NpciApiResponse.<TransactionResponse>builder()
                .success(false)
                .data(txResponse)
                .build();

        when(subscriptionRepository.findByStatusAndNextBillingDateLessThanEqual(eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(subscription));

        when(attemptRepository.save(any(SubscriptionPaymentAttempt.class))).thenReturn(attempt);

        when(npciServiceClient.initiateTransaction(anyString(), any(InitiateTransactionRequest.class)))
                .thenReturn(apiResponse);

        subscriptionService.executeScheduledPayments();

        verify(dunningService, times(1)).handleFailedAttempt(eq(subscription), contains("Insufficient funds"));
    }

    @Test
    void testExecuteScheduledPayments_DunningStep2() {
        Subscription subscription = Subscription.builder()
                .id(1L)
                .subscriptionId("SUB123")
                .userId("user123")
                .userUpiId("user@pay")
                .merchantUpiId("merchant@pay")
                .planName("Monthly Plan")
                .amount(new BigDecimal("99.00"))
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .nextBillingDate(LocalDate.now())
                .failedAttempts(1)
                .maxRetries(3)
                .build();

        SubscriptionPaymentAttempt attempt = SubscriptionPaymentAttempt.builder()
                .attemptId("ATT123")
                .status(PaymentAttemptStatus.PENDING)
                .build();

        TransactionResponse txResponse = TransactionResponse.builder()
                .status(TransactionStatus.FAILED)
                .npciResponseMessage("System Timeout")
                .build();

        NpciApiResponse<TransactionResponse> apiResponse = NpciApiResponse.<TransactionResponse>builder()
                .success(false)
                .data(txResponse)
                .build();

        when(subscriptionRepository.findByStatusAndNextBillingDateLessThanEqual(eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(subscription));

        when(attemptRepository.save(any(SubscriptionPaymentAttempt.class))).thenReturn(attempt);

        when(npciServiceClient.initiateTransaction(anyString(), any(InitiateTransactionRequest.class)))
                .thenReturn(apiResponse);

        subscriptionService.executeScheduledPayments();

        verify(dunningService, times(1)).handleFailedAttempt(eq(subscription), contains("System Timeout"));
    }

    @Test
    void testExecuteScheduledPayments_DunningStep3_Cancels() {
        Subscription subscription = Subscription.builder()
                .id(1L)
                .subscriptionId("SUB123")
                .userId("user123")
                .userUpiId("user@pay")
                .merchantUpiId("merchant@pay")
                .planName("Monthly Plan")
                .amount(new BigDecimal("99.00"))
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .nextBillingDate(LocalDate.now())
                .failedAttempts(2)
                .maxRetries(3)
                .build();

        SubscriptionPaymentAttempt attempt = SubscriptionPaymentAttempt.builder()
                .attemptId("ATT123")
                .status(PaymentAttemptStatus.PENDING)
                .build();

        TransactionResponse txResponse = TransactionResponse.builder()
                .status(TransactionStatus.DECLINED)
                .npciResponseMessage("Declined by Bank")
                .build();

        NpciApiResponse<TransactionResponse> apiResponse = NpciApiResponse.<TransactionResponse>builder()
                .success(false)
                .data(txResponse)
                .build();

        when(subscriptionRepository.findByStatusAndNextBillingDateLessThanEqual(eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(subscription));

        when(attemptRepository.save(any(SubscriptionPaymentAttempt.class))).thenReturn(attempt);

        when(npciServiceClient.initiateTransaction(anyString(), any(InitiateTransactionRequest.class)))
                .thenReturn(apiResponse);

        subscriptionService.executeScheduledPayments();

        verify(dunningService, times(1)).handleFailedAttempt(eq(subscription), contains("Declined by Bank"));
    }

    @Test
    void testPauseAndResume() {
        Subscription subscription = Subscription.builder()
                .subscriptionId("SUB123")
                .mandateId("MND123")
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionRepository.findBySubscriptionId("SUB123")).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionResponse response = subscriptionService.pauseSubscription("SUB123");
        assertEquals(SubscriptionStatus.PAUSED, response.getStatus());
        verify(npciServiceClient, times(1)).pauseMandate(anyString(), eq("MND123"));

        SubscriptionResponse resumeResponse = subscriptionService.resumeSubscription("SUB123");
        assertEquals(SubscriptionStatus.ACTIVE, resumeResponse.getStatus());
        assertEquals(LocalDate.now(), resumeResponse.getNextBillingDate());
    }
}
