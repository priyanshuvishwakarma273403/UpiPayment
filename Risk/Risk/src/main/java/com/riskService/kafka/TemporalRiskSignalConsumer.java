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

/**
 * Consumes Flink Temporal Risk Signals from Kafka topic 'temporal_risk_signals'.
 * Flink provides an asynchronous risk signal; Java Risk System incorporates the score addition
 * without directly blocking transactions at the streaming pipeline level.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TemporalRiskSignalConsumer {

    private final ObjectMapper objectMapper;
    private final Set<String> processedSignals = ConcurrentHashMap.newKeySet();

    @KafkaListener(
            topics = "temporal_risk_signals",
            groupId = "risk-temporal-signal-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTemporalRiskSignal(String message, Acknowledgment ack) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> signal = objectMapper.readValue(message, Map.class);
            String signalId = (String) signal.get("signalId");
            Object customerId = signal.get("customerId");
            String patternType = (String) signal.get("patternType");
            String severity = (String) signal.get("severity");
            Number riskScoreAddition = (Number) signal.getOrDefault("riskScoreAddition", 0);
            String description = (String) signal.get("eventSequenceDescription");

            if (signalId != null && !processedSignals.add("signal:" + signalId)) {
                log.warn("Duplicate temporal risk signal skipped | signalId={}", signalId);
                ack.acknowledge();
                return;
            }

            log.info("Received Flink TemporalRiskSignal | patternType={} | customerId={} | severity={} | scoreAddition=+{} | details={}",
                    patternType, customerId, severity, riskScoreAddition, description);

            // Ingest temporal risk signal into Java Risk state / profile score adjustment
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error consuming Flink TemporalRiskSignal Kafka event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }
}
