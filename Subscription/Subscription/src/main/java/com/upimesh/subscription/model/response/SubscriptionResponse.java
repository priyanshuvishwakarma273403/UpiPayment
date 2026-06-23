package com.upimesh.subscription.model.response;

import com.upimesh.subscription.model.enums.BillingCycle;
import com.upimesh.subscription.model.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private String subscriptionId;
    private String userId;
    private String userUpiId;
    private String merchantUpiId;
    private String merchantName;
    private String planName;
    private BigDecimal amount;
    private BillingCycle billingCycle;
    private String mandateId;
    private SubscriptionStatus status;
    private LocalDateTime trialEndsAt;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private LocalDate nextBillingDate;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private int failedAttempts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
