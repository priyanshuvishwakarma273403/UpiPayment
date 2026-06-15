package com.npci.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;
import java.util.Map;

/**
 * NotificationServiceClient — Send payment success/failure SMS/push notifications.
 *
 * Called after every NPCI transaction completes (success OR failure).
 * Notification service handles delivery — we just fire and forget.
 */
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
            String status,          // "SUCCESS", "FAILED", "DECLINED"
            String remarks,
            String npciResponseCode
    ) {}

    class NotificationFallback implements NotificationServiceClient {
        @Override
        public Map<String, Object> sendPaymentNotification(
                String serviceKey, PaymentNotificationRequest request) {
            // Notification failure should never block payment flow
            System.err.println("Notification fallback — txnId: " + request.transactionId());
            return Map.of("success", false, "queued", true);
        }
    }
}
