package com.fraudService.flink.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudService.flink.model.TemporalRiskSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemporalSignalPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public static final String TOPIC = "temporal_risk_signals";

    /**
     * Publishes TemporalRiskSignal to Kafka for downstream consumption by Java Risk System.
     */
    public void publishSignal(TemporalRiskSignal signal) {
        if (signal == null) return;
        try {
            String jsonPayload = objectMapper.writeValueAsString(signal);
            String key = signal.getCustomerId() != null ? String.valueOf(signal.getCustomerId()) : signal.getSignalId();

            kafkaTemplate.send(TOPIC, key, jsonPayload);
            log.info("Published TemporalRiskSignal to Kafka | topic={} | patternType={} | customerId={}",
                    TOPIC, signal.getPatternType(), signal.getCustomerId());
        } catch (Exception e) {
            log.error("Failed to publish TemporalRiskSignal to Kafka: {}", e.getMessage(), e);
        }
    }
}
