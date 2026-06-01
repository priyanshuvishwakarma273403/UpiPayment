package com.rewards_service.Rewards.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rewards_service.Rewards.service.RewardsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * ================================================================
 * Rewards Kafka Consumer
 * ================================================================
 * payment_completed consume karo -> points award karo
 * ================================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RewardsKafkaConsumer {

    private final RewardsService rewardsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment_completed", groupId = "rewards-payment-group",
            containerFactory = "kafkaListenerContainerFactory")
    @SuppressWarnings("unchecked")
    public void handlePaymentCompleted(String message, Acknowledgment ack) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            Long senderId = Long.valueOf(event.get("senderId").toString());
            BigDecimal amount = new BigDecimal(event.get("amount").toString());
            String paymentId = (String) event.get("paymentId");

            rewardsService.earnPointsForPayment(senderId, amount, paymentId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Rewards points award failed: {}", e.getMessage(), e);
            ack.acknowledge(); // Never block payment flow
        }
    }
}
