package com.analytics.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final Set<String> processedEvents = ConcurrentHashMap.newKeySet();

    @KafkaListener(
            topics = {"payment_completed", "payment_failed"},
            groupId = "analytics-metrics-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentMetrics(String message, Acknowledgment ack) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String paymentId = (String) event.get("paymentId");
            String eventId = (String) event.getOrDefault("eventId", paymentId);
            String correlationId = (String) event.getOrDefault("correlationId", paymentId);
            String status = (String) event.getOrDefault("paymentStatus", "COMPLETED");

            if (!processedEvents.add("analytics:" + eventId + ":" + paymentId)) {
                log.warn("[{}] Duplicate event skipped in Analytics consumer | eventId={} | paymentId={}", correlationId, eventId, paymentId);
                ack.acknowledge();
                return;
            }

            log.info("[{}] Analytics tracking metrics for paymentId={}, status={}, eventId={}", correlationId, paymentId, status, eventId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing Analytics Kafka event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }
}
