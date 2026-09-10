package com.paymentService.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * ================================================================
 * Payment Kafka Producer
 * ================================================================
 * Payment events Kafka topics par publish karta hai.
 *
 * Topics:
 * - payment_initiated: Fraud service consume karega (check karne ke liye)
 * - payment_completed: Notification service + Transaction service
 * - payment_failed: Notification service
 *
 * Pattern: Async event-driven architecture
 * Payment service directly fraud/notification service ko call NAHI karta.
 * Sirf event publish karta hai - baaki services apne time par consume karein.
 *
 * Benefits:
 * 1. Loose coupling - services independent hain
 * 2. Resilience - ek service down ho to doosri kaam karti rehti hai
 * 3. Scalability - consumers independently scale ho sakte hain
 * ================================================================
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // Topic names - constants
    public static final String TOPIC_PAYMENT_INITIATED = "payment_initiated";
    public static final String TOPIC_PAYMENT_COMPLETED = "payment_completed";
    public static final String TOPIC_PAYMENT_FAILED    = "payment_failed";
    public static final String TOPIC_PAYMENT_REVERSED  = "payment_reversed";
    public static final String TOPIC_FRAUD_DETECTED    = "fraud_detected";

    /**
     * Payment initiate hone par event publish karo
     * Key = paymentId (same key = same partition = ordering guarantee)
     */
    public void publishPaymentInitiated(PaymentEvent event){
        publishEvent(TOPIC_PAYMENT_INITIATED, event.getPaymentId(), event);
    }

    /**
     * Payment complete hone par event publish karo
     */
    public void publishPaymentCompleted(PaymentEvent event) {
        publishEvent(TOPIC_PAYMENT_COMPLETED, event.getPaymentId(), event);
    }

    /**
     * Payment fail hone par event publish karo
     */
    public void publishPaymentFailed(PaymentEvent event) {
        publishEvent(TOPIC_PAYMENT_FAILED, event.getPaymentId(), event);
    }

    /**
     * Payment reverse hone par event publish karo
     */
    public void publishPaymentReversed(PaymentEvent event) {
        publishEvent(TOPIC_PAYMENT_REVERSED, event.getPaymentId(), event);
    }

    /**
     * Generic event publish method
     * CompletableFuture ke through async send karta hai
     */
    private void publishEvent(String topic, String key, Object event){
        try{
            String eventJson = objectMapper.writeValueAsString(event);
            String correlationId = (event instanceof PaymentEvent pe && pe.getCorrelationId() != null) 
                    ? pe.getCorrelationId() : key;
            String eventId = (event instanceof PaymentEvent pe && pe.getEventId() != null) 
                    ? pe.getEventId() : "EVT-" + key;

            log.info("[{}] Publishing eventId={} to topic '{}' for key '{}'", correlationId, eventId, topic, key);

            // Async send - callback se success/failure pata chalega
            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(topic, key, eventJson);

            future.whenComplete((result, ex) -> {
                if(ex == null){
                    log.info("[{}] Event published to topic '{}', partition: {}, offset: {}",
                            correlationId,
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }else{
                    log.error("[{}] Failed to publish event to topic '{}': {}", correlationId, topic, ex.getMessage());
                }
            });
        }catch(Exception e){
            log.error("Error serializing event for topic '{}': {}", topic, e.getMessage());
            throw new RuntimeException("Failed to publish Kafka event", e);
        }
    }

}
