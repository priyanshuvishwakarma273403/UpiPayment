package com.transaction_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ================================================================
 * Transaction Kafka Consumer
 * ================================================================
 * Consumes:
 *   payment_completed -> MySQL ledger + MongoDB PaymentLog
 *   payment_failed    -> MySQL failed record + MongoDB log
 *   sync_completed    -> Offline payment ka record
 * ================================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionKafkaConsumer {

    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;
    private final java.util.Set<String> processedEvents = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    @KafkaListener(topics = "payment_completed", groupId = "transaction-completed-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentCompleted(String message, Acknowledgment ack) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String paymentId = (String) event.get("paymentId");
            String eventId = (String) event.getOrDefault("eventId", paymentId);
            String correlationId = (String) event.getOrDefault("correlationId", paymentId);

            if (!processedEvents.add("txn:completed:" + eventId + ":" + paymentId)) {
                log.warn("[{}] Duplicate completed payment event skipped | eventId={} | paymentId={}", correlationId, eventId, paymentId);
                ack.acknowledge();
                return;
            }

            log.info("[{}] Recording completed payment: paymentId={}, eventId={}", correlationId, paymentId, eventId);
            transactionService.recordCompletedPayment(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error recording completed payment: {}", e.getMessage(), e);
            ack.acknowledge(); // Prevent poison pill; use DLQ in production
        }
    }

    @KafkaListener(topics = "payment_failed", groupId = "transaction-failed-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentFailed(String message, Acknowledgment ack) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("Recording failed payment: {}", event.get("paymentId"));
            transactionService.recordFailedPayment(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error recording failed payment: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "sync_completed", groupId = "transaction-sync-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleSyncCompleted(String message, Acknowledgment ack) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("Recording synced offline payment: {}", event.get("paymentId"));
            // Same as completed - record the transaction
            transactionService.recordCompletedPayment(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error recording sync_completed: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

}
