package com.paymentService.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentService.entity.Payment;
import com.paymentService.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * ================================================================
 * Payment Service - Kafka Consumer
 * ================================================================
 * Consumes:
 * 1. fraud_detected -> Payment ko FAILED mark karo, wallet release karo
 *
 * Publish karta hai: PaymentKafkaProducer dekho
 * ================================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaConsumer {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final PaymentKafkaProducer kafkaProducer;

    /**
     * Fraud detected hone par payment BLOCKED karo
     */
    @KafkaListener(
            topics = "fraud_detected",
            groupId = "payment-fraud-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleFraudDetected(String message, Acknowledgment ack) {

        try{
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String paymentId = (String) event.get("paymentId");
            String reasons = (String) event.get("reasons");

            log.warn("Fraud detected for paymentId={}. Blocking payment.", paymentId);

            paymentRepository.findById(paymentId).ifPresent(payment -> {
                payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
                payment.setFraudStatus(Payment.FraudStatus.BLOCKED);
                payment.setFailureReason("FRAUD_BLOCKED: " + reasons);
                paymentRepository.save(payment);

                // Failure event publish karo (notification service ke liye)
                PaymentEvent failedEvent = PaymentEvent.fromPayment(payment);
                kafkaProducer.publishPaymentFailed(failedEvent);
                log.info("Payment {} marked as FAILED due to fraud", paymentId);

            });
            ack.acknowledge();
        }catch (Exception e){
            log.error("Error handling fraud_detected event: {}", e.getMessage(), e);
        }
    }
}
