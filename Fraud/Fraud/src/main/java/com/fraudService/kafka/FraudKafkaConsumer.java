package com.fraudService.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.dto.response.FraudIntelligenceResult;
import com.fraudService.dto.response.FraudRiskLevel;
import com.fraudService.service.FraudIntelligenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final FraudIntelligenceService intelligenceService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Set<String> processedEvents = ConcurrentHashMap.newKeySet();

    private static final String TOPIC_FRAUD_DETECTED = "fraud_detected";

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
            if (paymentId == null) paymentId = (String) event.get("transactionId");

            String eventId = (String) event.getOrDefault("eventId", paymentId);
            String correlationId = (String) event.getOrDefault("correlationId", paymentId);

            if (!processedEvents.add("fraud:" + eventId + ":" + paymentId)) {
                log.warn("[{}] Duplicate event skipped in Fraud consumer | eventId={} | paymentId={}", correlationId, eventId, paymentId);
                ack.acknowledge();
                return;
            }

            log.info("[{}] Real-time Fraud Intelligence evaluating paymentId={}", correlationId, paymentId);

            // Construct FraudCheckRequest
            BigDecimal amount = BigDecimal.ZERO;
            if (event.get("amount") != null) {
                try {
                    amount = new BigDecimal(event.get("amount").toString());
                } catch (Exception ignored) {}
            }

            FraudCheckRequest req = FraudCheckRequest.builder()
                    .paymentId(paymentId)
                    .senderId(event.get("senderId") != null ? Long.valueOf(event.get("senderId").toString()) : 1L)
                    .receiverId(event.get("receiverId") != null ? Long.valueOf(event.get("receiverId").toString()) : 2L)
                    .senderUpiId((String) event.getOrDefault("senderUpiId", "sender@upimesh"))
                    .receiverUpiId((String) event.getOrDefault("receiverUpiId", "receiver@upimesh"))
                    .amount(amount)
                    .paymentMode((String) event.getOrDefault("paymentMode", "UPI"))
                    .deviceId((String) event.getOrDefault("deviceId", "DEV_DEFAULT"))
                    .ipAddress((String) event.getOrDefault("ipAddress", "127.0.0.1"))
                    .build();

            // Real-time evaluation
            FraudIntelligenceResult result = intelligenceService.evaluateFraudIntelligence(req);

            if (result.getRiskLevel() == FraudRiskLevel.REQUIRES_INVESTIGATION || result.getRiskLevel() == FraudRiskLevel.HIGH_RISK) {
                log.warn("[{}] CRITICAL FRAUD DETECTED | paymentId={} | decision={} | score={}",
                        correlationId, paymentId, result.getDecision(), result.getRiskScore());

                Map<String, Object> alertPayload = new HashMap<>();
                alertPayload.put("paymentId", paymentId);
                alertPayload.put("correlationId", correlationId);
                alertPayload.put("riskScore", result.getRiskScore());
                alertPayload.put("riskLevel", result.getRiskLevel().name());
                alertPayload.put("decision", result.getDecision());
                alertPayload.put("explanation", result.getExplanation());
                alertPayload.put("evidence", result.getEvidence());

                kafkaTemplate.send(TOPIC_FRAUD_DETECTED, paymentId, objectMapper.writeValueAsString(alertPayload));
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing Fraud Kafka event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }
}
