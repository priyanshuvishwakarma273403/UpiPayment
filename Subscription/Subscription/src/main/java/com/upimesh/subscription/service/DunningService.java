package com.upimesh.subscription.service;

import com.upimesh.subscription.model.entity.Subscription;
import com.upimesh.subscription.model.enums.SubscriptionStatus;
import com.upimesh.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DunningService {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * Handles subscription payment failure and implements the RBI-compliant dunning flow.
     * Retries:
     * - Attempt 1 fail: Retry in 1 day.
     * - Attempt 2 fail: Retry in 3 days.
     * - Attempt 3 fail: Cancel subscription (status = PAYMENT_FAILED).
     *
     * @param subscription  the subscription that failed payment
     * @param failureReason details of why the payment failed
     */
    public void handleFailedAttempt(Subscription subscription, String failureReason) {
        int currentFailed = subscription.getFailedAttempts();
        int newFailed = currentFailed + 1;
        subscription.setFailedAttempts(newFailed);

        log.warn("Handling dunning for subscriptionId: {}. Attempt {} failed. Reason: {}", 
                subscription.getSubscriptionId(), newFailed, failureReason);

        if (newFailed >= subscription.getMaxRetries()) {
            subscription.setStatus(SubscriptionStatus.PAYMENT_FAILED);
            subscription.setCancelledAt(LocalDateTime.now());
            subscription.setCancelReason("Dunning limit reached. Third failed attempt: " + failureReason);
            subscription.setNextBillingDate(null);
            log.error("Subscription {} permanently marked as PAYMENT_FAILED after {} failed attempts.", 
                    subscription.getSubscriptionId(), subscription.getMaxRetries());
            notifyParties(subscription, "Subscription permanently cancelled due to payment failure: " + failureReason);
        } else if (newFailed == 1) {
            subscription.setNextBillingDate(LocalDate.now().plusDays(1));
            log.info("Subscription {} scheduled for retry in 1 day (date: {})", 
                    subscription.getSubscriptionId(), subscription.getNextBillingDate());
            notifyParties(subscription, "First payment attempt failed. Retrying in 1 day.");
        } else if (newFailed == 2) {
            subscription.setNextBillingDate(LocalDate.now().plusDays(3));
            log.info("Subscription {} scheduled for retry in 3 days (date: {})", 
                    subscription.getSubscriptionId(), subscription.getNextBillingDate());
            notifyParties(subscription, "Second payment attempt failed. Retrying in 3 days.");
        }

        subscriptionRepository.save(subscription);
    }

    private void notifyParties(Subscription subscription, String message) {
        log.info("NOTIFICATION [USER]: Sent to {} (UPI ID: {}) - {}", subscription.getUserId(), subscription.getUserUpiId(), message);
        log.info("NOTIFICATION [MERCHANT]: Sent to {} (UPI ID: {}) - {}", subscription.getMerchantName(), subscription.getMerchantUpiId(), message);
    }
}
