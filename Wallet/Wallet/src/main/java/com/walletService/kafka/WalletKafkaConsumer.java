package com.walletService.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walletService.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * ================================================================
 * Wallet Kafka Consumer
 * ================================================================
 * payment_initiated consume karta hai:
 *   -> Sender ka wallet debit karo
 *   -> Receiver ka wallet credit karo
 *   -> payment_completed ya payment_failed publish karo
 *
 * Note: Fraud check PEHLE hota hai (fraud-service),
 * wallet debit BAAD mein hota hai.
 * Agar fraud detected hua to payment_failed milega - credit nahi hoga.
 * ================================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WalletKafkaConsumer {

    private final WalletService walletService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_PAYMENT_COMPLETED = "payment_completed";
    private static final String TOPIC_PAYMENT_FAILED    = "payment_failed";

    private final java.util.Set<String> processedEvents = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    /**
     * payment_initiated topic consume karo aur wallet debit+credit karo
     */
    @KafkaListener(
            topics = "payment_initiated",
            groupId = "wallet_payment_group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentInitiated(String message, Acknowledgment ack) {
        String paymentId = "UNKNOWN";
        try{
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            paymentId        = (String) event.get("paymentId");
            String eventId   = (String) event.getOrDefault("eventId", paymentId);
            String correlationId = (String) event.getOrDefault("correlationId", paymentId);
            Long senderId    = Long.valueOf(event.get("senderId").toString());
            Long receiverId  = Long.valueOf(event.get("receiverId").toString());
            BigDecimal amount = new BigDecimal(event.get("amount").toString());
            String fraudStatus = (String) event.getOrDefault("fraudStatus", "SAFE");

            // Idempotency check: duplicate event skip karo
            if (!processedEvents.add("evt:" + eventId + ":" + paymentId)) {
                log.warn("[{}] Duplicate event skipped in Wallet | eventId={} | paymentId={}", correlationId, eventId, paymentId);
                ack.acknowledge();
                return;
            }

            log.info("[{}] Processing wallet for paymentId={}, senderId={}, receiverId={}, amount={}",
                    correlationId, paymentId, senderId, receiverId, amount);

            // Fraud blocked hai to process mat karo
            if ("BLOCKED".equals(fraudStatus)) {
                log.warn("Payment {} is FRAUD_BLOCKED - skipping wallet processing", paymentId);
                ack.acknowledge();
                return;
            }

            // ReceiverId -1 hai to UPI resolve pending hai - skip
            if (receiverId == -1L) {
                log.warn("Payment {} has unresolved receiverId - skipping", paymentId);
                ack.acknowledge();
                return;
            }

            // Step 1: Sender debit karo
            walletService.debitWallet(senderId, amount, paymentId);

            // Step 2: Receiver credit karo
            walletService.creditWallet(receiverId, amount, paymentId);

            // Step 3: payment_completed publish karo
            publishEvent(TOPIC_PAYMENT_COMPLETED, paymentId, event, "SUCCESS");
            log.info("Payment {} processed successfully - debit+credit done", paymentId);

            ack.acknowledge();

        } catch (Exception e) {
            log.error("Error processing wallet for paymentId={}: {}", paymentId, e.getMessage(), e);
            // payment_failed publish karo
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> event = objectMapper.readValue(message, Map.class);
                publishEvent(TOPIC_PAYMENT_FAILED, paymentId, event, "FAILED");
            } catch (Exception ex) {
                log.error("Could not publish payment_failed event: {}", ex.getMessage());
            }
            ack.acknowledge(); // Acknowledge to prevent infinite retry (DLQ use karo production mein)
        }
    }

    private void publishEvent(String topic, String paymentId,
                              Map<String, Object> originalEvent, String status) {
        try {
            originalEvent.put("paymentStatus", status);
            originalEvent.put("processedBy", "wallet-service");
            String json = objectMapper.writeValueAsString(originalEvent);
            kafkaTemplate.send(topic, paymentId, json);
            log.info("Published {} event for paymentId={}", topic, paymentId);
        } catch (Exception e) {
            log.error("Failed to publish {} event: {}", topic, e.getMessage());
        }
    }
}
