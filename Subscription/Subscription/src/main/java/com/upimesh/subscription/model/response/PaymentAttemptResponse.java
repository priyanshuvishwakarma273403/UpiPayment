package com.upimesh.subscription.model.response;

import com.upimesh.subscription.model.enums.PaymentAttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAttemptResponse {
    private String attemptId;
    private String subscriptionId;
    private String transactionId;
    private BigDecimal amount;
    private PaymentAttemptStatus status;
    private int attemptNumber;
    private String failureReason;
    private LocalDateTime attemptedAt;
    private LocalDateTime completedAt;
}
