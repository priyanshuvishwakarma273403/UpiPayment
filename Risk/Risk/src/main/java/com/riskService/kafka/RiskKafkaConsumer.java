package com.riskService.kafka;

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
public class RiskKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final Set<String> processedEvents = ConcurrentHashMap.newKeySet();

    @KafkaListener(
            topics = "payment_completed",
            groupId = "risk-profiling-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(String message, Acknowledgment ack) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String paymentId = (String) event.get("paymentId");
            String eventId = (String) event.getOrDefault("eventId", paymentId);
            String correlationId = (String) event.getOrDefault("correlationId", paymentId);

            if (!processedEvents.add("risk:" + eventId + ":" + paymentId)) {
                log.warn("[{}] Duplicate event skipped in Risk consumer | eventId={} | paymentId={}", correlationId, eventId, paymentId);
                ack.acknowledge();
                return;
            }

            log.info("[{}] Updating async user risk profile for paymentId={}, eventId={}", correlationId, paymentId, eventId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing Risk Kafka event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }
}
