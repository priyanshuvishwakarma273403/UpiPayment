package com.fraudService.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudService.service.FraudRuleEngine;
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
public class FraudKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final Set<String> processedEvents = ConcurrentHashMap.newKeySet();

    @KafkaListener(
            topics = "payment_initiated",
            groupId = "fraud-detection-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentInitiated(String message, Acknowledgment ack) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String paymentId = (String) event.get("paymentId");
            String eventId = (String) event.getOrDefault("eventId", paymentId);
            String correlationId = (String) event.getOrDefault("correlationId", paymentId);

            if (!processedEvents.add("fraud:" + eventId + ":" + paymentId)) {
                log.warn("[{}] Duplicate event skipped in Fraud consumer | eventId={} | paymentId={}", correlationId, eventId, paymentId);
                ack.acknowledge();
                return;
            }

            log.info("[{}] Fraud service tracking eventId={} for paymentId={}", correlationId, eventId, paymentId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing Fraud Kafka event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }
}
