package com.upimesh.dispute.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(
        name = "notification-service",
        fallback = NotificationServiceClient.NotificationFallback.class
)
public interface NotificationServiceClient {

    @PostMapping("/notification/internal/payment")
    Map<String, Object> sendPaymentNotification(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestBody PaymentNotificationRequest request
    );

    record PaymentNotificationRequest(
            String transactionId,
            String senderUpiId,
            String receiverUpiId,
            BigDecimal amount,
            String status,          // e.g. "DISPUTE_RAISED", "DISPUTE_MERCHANT_NOTIFIED", "DISPUTE_RESOLVED"
            String remarks,
            String npciResponseCode
    ) {}

    class NotificationFallback implements NotificationServiceClient {
        @Override
        public Map<String, Object> sendPaymentNotification(
                String serviceKey, PaymentNotificationRequest request) {
            System.err.println("Notification fallback triggered — failed to notify for txnId: " + request.transactionId());
            return Map.of("success", false, "queued", true);
        }
    }
}
