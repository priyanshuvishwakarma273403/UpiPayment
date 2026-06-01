package com.notification_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification_service.dto.NotificationDto;
import com.notification_service.service.EmailNotificationService;
import com.notification_service.service.SmsNotificationService;
import com.notification_service.service.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Notification Kafka Consumer
 * Consumes: payment_completed, payment_failed, sync_completed
 * Sends: Email (Thymeleaf HTML) + SMS (MSG91/Mock)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaConsumer {

    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;
    private final ObjectMapper objectMapper;
    private final AuthServiceClient authServiceClient;

    @KafkaListener(topics = "payment_completed", groupId = "notification-completed-group",
    containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentCompleted(String message,
                                       @Header(KafkaHeaders.OFFSET) long offset, Acknowledgment ack){

        try{
            NotificationDto dto = parseEvent(message);
            log.info("payment_completed notification: paymentId={}", dto.getPaymentId());
            emailService.sendPaymentSuccessToSender(dto);
            emailService.sendPaymentReceiverToReceiver(dto);
            smsService.sendPaymentSuccessSms(dto);
            smsService.sendPaymentReceiveSms(dto);
            ack.acknowledge();
        }catch (Exception e){
            log.error("payment_completed notification error: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "payment_failed", groupId = "notification-failed-group",
    containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentFailed(String message,
                                    @Header(KafkaHeaders.OFFSET) long offset, Acknowledgment ack){
        try{
            NotificationDto dto = parseEvent(message);
            log.info("payment_failed notification: paymentId={}", dto.getPaymentId());
            emailService.sendPaymentFailed(dto);
            smsService.sendPaymentFailedSms(dto);
            ack.acknowledge();
        }catch (Exception e){
            log.error("sync_completed notification error: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "sync_completed", groupId = "notification-sync-group",
    containerFactory = "kafkaListenerContainerFactory")
    public void handleSyncComplete(String message,
                                   @Header(KafkaHeaders.OFFSET) long offset, Acknowledgment ack){
        try{
            NotificationDto dto = parseEvent(message);
            log.info("sync_completed notification: paymentId={}", dto.getPaymentId());
            emailService.sendSyncCompleted(dto);
            smsService.sendSyncSuccessSms(dto);
            ack.acknowledge();
        }catch (Exception e){
            log.error("sync_completed notification error: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }



    private NotificationDto parseEvent(String message) throws Exception{
        Map<String , Object> ev = objectMapper.readValue(message, Map.class);
        NotificationDto dto = NotificationDto.builder()
                .paymentId(  str(ev,"paymentId"))
                .senderId(   lng(ev,"senderId"))
                .receiverId( lng(ev,"receiverId"))
                .senderUpiId(str(ev,"senderUpiId"))
                .receiverUpiId(str(ev,"receiverUpiId"))
                .amount(     bdc(ev,"amount"))
                .paymentStatus(str(ev,"paymentStatus"))
                .paymentMode(str(ev,"paymentMode"))
                .description(str(ev,"description"))
                .failureReason(str(ev,"failureReason"))
                .senderEmail(str(ev,"senderEmail"))
                .receiverEmail(str(ev,"receiverEmail"))
                .senderPhone(str(ev,"senderPhone"))
                .receiverPhone(str(ev,"receiverPhone"))
                .senderName( str(ev,"senderName"))
                .receiverName(str(ev,"receiverName"))
                .timestamp(LocalDateTime.now())
                .build();

        // Enrich sender details if missing
        if (dto.getSenderEmail() == null || dto.getSenderEmail().isBlank()) {
            Map<String, Object> senderDetails = null;
            if (dto.getSenderId() != null) {
                senderDetails = authServiceClient.getUserDetails(dto.getSenderId());
            } else if (dto.getSenderUpiId() != null) {
                senderDetails = authServiceClient.getUserDetailsByUpiId(dto.getSenderUpiId());
            }
            if (senderDetails != null) {
                dto.setSenderEmail(str(senderDetails, "email"));
                dto.setSenderName(str(senderDetails, "fullName"));
                dto.setSenderPhone(str(senderDetails, "phoneNumber"));
            }
        }

        // Enrich receiver details if missing
        if (dto.getReceiverEmail() == null || dto.getReceiverEmail().isBlank()) {
            Map<String, Object> receiverDetails = null;
            if (dto.getReceiverId() != null && dto.getReceiverId() != -1L) {
                receiverDetails = authServiceClient.getUserDetails(dto.getReceiverId());
            } else if (dto.getReceiverUpiId() != null) {
                receiverDetails = authServiceClient.getUserDetailsByUpiId(dto.getReceiverUpiId());
            }
            if (receiverDetails != null) {
                dto.setReceiverEmail(str(receiverDetails, "email"));
                dto.setReceiverName(str(receiverDetails, "fullName"));
                dto.setReceiverPhone(str(receiverDetails, "phoneNumber"));
            }
        }

        return dto;
    }

    private String     str(Map<String,Object> m, String k) { Object v=m.get(k); return v!=null?v.toString():null; }
    private Long       lng(Map<String,Object> m, String k) { Object v=m.get(k); return v!=null?Long.valueOf(v.toString()):null; }
    private BigDecimal bdc(Map<String,Object> m, String k) { Object v=m.get(k); return v!=null?new BigDecimal(v.toString()):BigDecimal.ZERO;

    }
}
