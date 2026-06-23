package com.upimesh.subscription.service;

import com.upimesh.subscription.exception.SubscriptionNotFoundException;
import com.upimesh.subscription.feign.NpciServiceClient;
import com.upimesh.subscription.feign.dto.*;
import com.upimesh.subscription.model.entity.Subscription;
import com.upimesh.subscription.model.entity.SubscriptionPaymentAttempt;
import com.upimesh.subscription.model.enums.BillingCycle;
import com.upimesh.subscription.model.enums.PaymentAttemptStatus;
import com.upimesh.subscription.model.enums.SubscriptionStatus;
import com.upimesh.subscription.model.request.CreateSubscriptionRequest;
import com.upimesh.subscription.model.response.PaymentAttemptResponse;
import com.upimesh.subscription.model.response.SubscriptionResponse;
import com.upimesh.subscription.repository.SubscriptionPaymentAttemptRepository;
import com.upimesh.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPaymentAttemptRepository attemptRepository;
    private final NpciServiceClient npciServiceClient;
    private final DunningService dunningService;

    @Value("${subscription.internal-key:internal-secret-change-in-prod}")
    private String internalKey;

    /**
     * Initiates and creates a subscription mandate in both local DB and NPCI.
     */
    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        log.info("Initiating subscription creation for user: {} to merchant: {}", 
                request.getUserId(), request.getMerchantUpiId());

        String subscriptionId = "SUB" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 15).toUpperCase();

        MandateCreateRequest mandateRequest = MandateCreateRequest.builder()
                .userUpiId(request.getUserUpiId())
                .merchantUpiId(request.getMerchantUpiId())
                .merchantName(request.getMerchantName())
                .maxAmount(request.getAmount())
                .frequency(mapBillingCycleToFrequency(request.getBillingCycle()))
                .executionDay(LocalDate.now().getDayOfMonth())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(5))
                .purpose("SUBSCRIPTION")
                .build();

        NpciApiResponse<MandateResponse> npciResponse;
        try {
            npciResponse = npciServiceClient.createMandate(internalKey, mandateRequest);
        } catch (Exception e) {
            log.error("Feign exception during NPCI mandate creation: {}", e.getMessage());
            throw new RuntimeException("NPCI mandate registration failed: " + e.getMessage());
        }

        if (npciResponse == null || !npciResponse.isSuccess() || npciResponse.getData() == null) {
            String errorMsg = npciResponse != null ? npciResponse.getMessage() : "Null response from NPCI";
            log.error("NPCI mandate creation returned failure: {}", errorMsg);
            throw new RuntimeException("NPCI mandate registration failed: " + errorMsg);
        }

        LocalDate nextBilling = calculateNextBillingDate(LocalDate.now(), request.getBillingCycle());

        Subscription subscription = Subscription.builder()
                .subscriptionId(subscriptionId)
                .userId(request.getUserId())
                .userUpiId(request.getUserUpiId())
                .merchantUpiId(request.getMerchantUpiId())
                .merchantName(request.getMerchantName())
                .planName(request.getPlanName())
                .amount(request.getAmount())
                .billingCycle(request.getBillingCycle())
                .mandateId(npciResponse.getData().getMandateId())
                .status(SubscriptionStatus.ACTIVE)
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(nextBilling.atStartOfDay())
                .nextBillingDate(nextBilling)
                .failedAttempts(0)
                .maxRetries(3)
                .build();

        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription created successfully with ID: {} and mandateID: {}", 
                subscriptionId, subscription.getMandateId());

        return mapToSubscriptionResponse(subscription);
    }

    /**
     * Pauses an active subscription.
     */
    @Transactional
    public SubscriptionResponse pauseSubscription(String subscriptionId) {
        log.info("Pausing subscription with ID: {}", subscriptionId);
        Subscription subscription = getSubscriptionOrThrow(subscriptionId);

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE subscriptions can be paused. Current status: " + subscription.getStatus());
        }

        try {
            npciServiceClient.pauseMandate(internalKey, subscription.getMandateId());
        } catch (Exception e) {
            log.error("NPCI mandate pause failed for mandate: {}", subscription.getMandateId(), e);
            throw new RuntimeException("Failed to pause mandate in NPCI: " + e.getMessage());
        }

        subscription.setStatus(SubscriptionStatus.PAUSED);
        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription {} paused successfully.", subscriptionId);

        return mapToSubscriptionResponse(subscription);
    }

    /**
     * Resumes a paused subscription.
     */
    @Transactional
    public SubscriptionResponse resumeSubscription(String subscriptionId) {
        log.info("Resuming subscription with ID: {}", subscriptionId);
        Subscription subscription = getSubscriptionOrThrow(subscriptionId);

        if (subscription.getStatus() != SubscriptionStatus.PAUSED) {
            throw new IllegalStateException("Only PAUSED subscriptions can be resumed. Current status: " + subscription.getStatus());
        }

        // Resumes are resolved locally, the mandate can accept transactions again.
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setFailedAttempts(0);
        subscription.setNextBillingDate(LocalDate.now()); // Run charge attempt as soon as possible
        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription {} resumed successfully.", subscriptionId);

        return mapToSubscriptionResponse(subscription);
    }

    /**
     * Cancels a subscription.
     */
    @Transactional
    public SubscriptionResponse cancelSubscription(String subscriptionId, String reason) {
        log.info("Cancelling subscription with ID: {} | Reason: {}", subscriptionId, reason);
        Subscription subscription = getSubscriptionOrThrow(subscriptionId);

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED || subscription.getStatus() == SubscriptionStatus.PAYMENT_FAILED) {
            throw new IllegalStateException("Subscription is already inactive.");
        }

        try {
            npciServiceClient.revokeMandate(internalKey, subscription.getMandateId());
        } catch (Exception e) {
            log.error("NPCI mandate revoke failed for mandate: {}", subscription.getMandateId(), e);
            // We still proceed to cancel locally to prevent unauthorized future charges
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setCancelReason(reason);
        subscription.setNextBillingDate(null);
        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription {} cancelled successfully.", subscriptionId);

        return mapToSubscriptionResponse(subscription);
    }

    /**
     * Scheduler to execute payments daily at 6:00 AM.
     */
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void executeScheduledPayments() {
        LocalDate today = LocalDate.now();
        log.info("Executing scheduled subscription auto-debit payments for date: {}", today);

        List<Subscription> dueSubscriptions = subscriptionRepository
                .findByStatusAndNextBillingDateLessThanEqual(SubscriptionStatus.ACTIVE, today);

        log.info("Found {} active subscriptions due for auto-debit today", dueSubscriptions.size());

        for (Subscription sub : dueSubscriptions) {
            executeSinglePayment(sub);
        }
    }

    private void executeSinglePayment(Subscription subscription) {
        String attemptId = "ATT" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 15).toUpperCase();

        int nextAttemptNumber = subscription.getFailedAttempts() + 1;

        SubscriptionPaymentAttempt attempt = SubscriptionPaymentAttempt.builder()
                .attemptId(attemptId)
                .subscriptionId(subscription.getSubscriptionId())
                .amount(subscription.getAmount())
                .status(PaymentAttemptStatus.PENDING)
                .attemptNumber(nextAttemptNumber)
                .attemptedAt(LocalDateTime.now())
                .build();

        attempt = attemptRepository.save(attempt);

        InitiateTransactionRequest txnReq = InitiateTransactionRequest.builder()
                .senderUpiId(subscription.getUserUpiId())
                .receiverUpiId(subscription.getMerchantUpiId())
                .amount(subscription.getAmount())
                .remarks("AutoPay: " + subscription.getPlanName())
                .type(TransactionType.MANDATE)
                .deviceId("SYSTEM")
                .ipAddress("127.0.0.1")
                .mpinHash("MANDATE_HASH") // Pre-authorized mandate token bypassing pin checks
                .idempotencyKey(attemptId)
                .build();

        try {
            NpciApiResponse<TransactionResponse> response = npciServiceClient.initiateTransaction(internalKey, txnReq);
            if (response != null && response.isSuccess() && response.getData() != null && 
                    response.getData().getStatus() == TransactionStatus.SUCCESS) {
                
                TransactionResponse txData = response.getData();
                attempt.setStatus(PaymentAttemptStatus.SUCCESS);
                attempt.setTransactionId(txData.getTransactionId());
                attempt.setCompletedAt(LocalDateTime.now());
                attemptRepository.save(attempt);

                // Advance subscription timeline
                LocalDate nextBilling = calculateNextBillingDate(LocalDate.now(), subscription.getBillingCycle());
                subscription.setFailedAttempts(0);
                subscription.setCurrentPeriodStart(LocalDateTime.now());
                subscription.setCurrentPeriodEnd(nextBilling.atStartOfDay());
                subscription.setNextBillingDate(nextBilling);
                subscriptionRepository.save(subscription);

                log.info("Auto-debit success for subscription: {} | TxnId: {}", 
                        subscription.getSubscriptionId(), txData.getTransactionId());
            } else {
                String failureReason = response != null && response.getData() != null 
                        ? response.getData().getNpciResponseMessage() 
                        : (response != null ? response.getMessage() : "Unknown failure");
                
                handlePaymentFailure(subscription, attempt, failureReason);
            }
        } catch (Exception e) {
            log.error("Exception during auto-debit transaction for subscription: {}", subscription.getSubscriptionId(), e);
            handlePaymentFailure(subscription, attempt, "Exception occurred: " + e.getMessage());
        }
    }

    private void handlePaymentFailure(Subscription subscription, SubscriptionPaymentAttempt attempt, String failureReason) {
        attempt.setStatus(PaymentAttemptStatus.FAILED);
        attempt.setFailureReason(failureReason);
        attempt.setCompletedAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        dunningService.handleFailedAttempt(subscription, failureReason);
    }

    public SubscriptionResponse getSubscription(String subscriptionId) {
        return mapToSubscriptionResponse(getSubscriptionOrThrow(subscriptionId));
    }

    public List<SubscriptionResponse> getUserSubscriptions(String userId) {
        return subscriptionRepository.findByUserId(userId).stream()
                .map(this::mapToSubscriptionResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentAttemptResponse> getPaymentAttempts(String subscriptionId) {
        return attemptRepository.findBySubscriptionId(subscriptionId).stream()
                .map(this::mapToPaymentAttemptResponse)
                .collect(Collectors.toList());
    }

    public LocalDate calculateNextBillingDate(LocalDate baseDate, BillingCycle cycle) {
        return switch (cycle) {
            case DAILY -> baseDate.plusDays(1);
            case WEEKLY -> baseDate.plusWeeks(1);
            case MONTHLY -> baseDate.plusMonths(1);
            case QUARTERLY -> baseDate.plusMonths(3);
            case YEARLY -> baseDate.plusYears(1);
        };
    }

    private String mapBillingCycleToFrequency(BillingCycle cycle) {
        return switch (cycle) {
            case DAILY -> "DAILY";
            case WEEKLY -> "WEEKLY";
            case MONTHLY -> "MONTHLY";
            default -> "AS_PRESENTED";
        };
    }

    private Subscription getSubscriptionOrThrow(String subscriptionId) {
        return subscriptionRepository.findBySubscriptionId(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found with ID: " + subscriptionId));
    }

    private SubscriptionResponse mapToSubscriptionResponse(Subscription sub) {
        return SubscriptionResponse.builder()
                .subscriptionId(sub.getSubscriptionId())
                .userId(sub.getUserId())
                .userUpiId(sub.getUserUpiId())
                .merchantUpiId(sub.getMerchantUpiId())
                .merchantName(sub.getMerchantName())
                .planName(sub.getPlanName())
                .amount(sub.getAmount())
                .billingCycle(sub.getBillingCycle())
                .mandateId(sub.getMandateId())
                .status(sub.getStatus())
                .trialEndsAt(sub.getTrialEndsAt())
                .currentPeriodStart(sub.getCurrentPeriodStart())
                .currentPeriodEnd(sub.getCurrentPeriodEnd())
                .nextBillingDate(sub.getNextBillingDate())
                .cancelledAt(sub.getCancelledAt())
                .cancelReason(sub.getCancelReason())
                .failedAttempts(sub.getFailedAttempts())
                .createdAt(sub.getCreatedAt())
                .updatedAt(sub.getUpdatedAt())
                .build();
    }

    private PaymentAttemptResponse mapToPaymentAttemptResponse(SubscriptionPaymentAttempt attempt) {
        return PaymentAttemptResponse.builder()
                .attemptId(attempt.getAttemptId())
                .subscriptionId(attempt.getSubscriptionId())
                .transactionId(attempt.getTransactionId())
                .amount(attempt.getAmount())
                .status(attempt.getStatus())
                .attemptNumber(attempt.getAttemptNumber())
                .failureReason(attempt.getFailureReason())
                .attemptedAt(attempt.getAttemptedAt())
                .completedAt(attempt.getCompletedAt())
                .build();
    }
}
