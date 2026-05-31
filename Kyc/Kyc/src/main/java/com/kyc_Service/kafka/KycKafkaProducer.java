package com.kyc_Service.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ================================================================
 * KYC Kafka Producer
 * ================================================================
 * KYC events publish karta hai jab verification complete/fail ho.
 *
 * Topics:
 *   kyc_completed -> auth-service   (user limits update)
 *                    wallet-service  (monthly limit unlock)
 *                    notification    (success email/SMS)
 *
 *   kyc_expired   -> notification-service (re-verify reminder)
 *                    auth-service          (limit downgrade)
 *
 *   kyc_failed    -> notification-service (failure alert)
 *
 * Pattern: Fire-and-forget with async callback logging.
 * ================================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KycKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_KYC_COMPLETED = "kyc_completed";
    private static final String TOPIC_KYC_EXPIRED   = "kyc_expired";
    private static final String TOPIC_KYC_FAILED    = "kyc_failed";

    /**
     * KYC verification complete hone par publish karo
     * Wallet-service subscribe karta hai - monthly limit unlock karega
     */
    public void publishKycCompleted(Long userId, String kycLevel) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId",    userId);
        payload.put("kycLevel",  kycLevel);
        payload.put("status",    "COMPLETED");
        payload.put("timestamp", LocalDateTime.now().toString());

        publish(TOPIC_KYC_COMPLETED, userId.toString(), payload);
        log.info("Published kyc_completed: userId={}, level={}", userId, kycLevel);
    }

    /**
     * KYC expire hone par publish karo (10 years baad)
     * Notification-service reminder bhejega
     */
    public void publishKycExpired(Long userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId",    userId);
        payload.put("status",    "EXPIRED");
        payload.put("message",   "Your KYC has expired. Please re-verify to continue unlimited transactions.");
        payload.put("timestamp", LocalDateTime.now().toString());

        publish(TOPIC_KYC_EXPIRED, userId.toString(), payload);
        log.info("Published kyc_expired: userId={}", userId);
    }

    /**
     * KYC verification fail hone par publish karo
     * Notification-service alert bhejega
     */
    public void publishKycFailed(Long userId, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId",    userId);
        payload.put("status",    "FAILED");
        payload.put("reason",    reason);
        payload.put("timestamp", LocalDateTime.now().toString());

        publish(TOPIC_KYC_FAILED, userId.toString(), payload);
        log.warn("Published kyc_failed: userId={}, reason={}", userId, reason);
    }

    /**
     * Generic publish method with async callback
     */
    private void publish(String topic, String key, Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(topic, key, json);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("KYC event sent: topic={}, partition={}, offset={}",
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("KYC event send failed: topic={}, key={}, error={}",
                            topic, key, ex.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("Failed to serialize KYC event for topic={}: {}", topic, e.getMessage());
        }
    }
}
