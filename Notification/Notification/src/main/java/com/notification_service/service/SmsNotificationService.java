package com.notification_service.service;

import com.notification_service.dto.NotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * ================================================================
 * SMS Notification Service
 * ================================================================
 * SMS bhejna ke liye yeh service hai.
 *
 * Production mein yeh providers use hote hain India mein:
 * - MSG91 (https://msg91.com) - sabse popular India mein
 * - Twilio (international)
 * - AWS SNS
 * - Fast2SMS (cheapest)
 *
 * Abhi: Mock implementation (console log)
 * Production ke liye: MSG91 ka REST API call karo
 *   POST https://api.msg91.com/api/v5/otp
 *   Headers: authkey: YOUR_AUTH_KEY
 *
// * @Async: SMS sending blocking nahi hoga - alag thread mein chalega
// * @Retryable: SMS fail hone par 3 baar retry karega (2 sec backoff)
 * ================================================================
 */
@Service
@Slf4j
public class SmsNotificationService {

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${notification.sms.provider:MOCK}")
    private String smsProvider;

    @Value("${notification.sms.msg91.authkey:}")
    private String msg91AuthKey;

    @Value("${notification.sms.msg91.senderId:UPIMSH}")
    private String senderId;

    // ================================================================
    // 1. PAYMENT SUCCESS SMS
    // ================================================================

    /**
     * Payment success hone par sender ko SMS bhejo
     */
    @Async("notificationExecutor")
    @Retryable(retryFor = Exception.class, maxAttempts = 3,
    backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendPaymentSuccessSms(NotificationDto dto){

        String message =   buildPaymentSuccessMessage(dto);
        String phone = dto.getSenderPhone();

        if(phone == null || phone.isBlank()){
            log.warn("No phone number available for userId={}, skipping SMS", dto.getSenderId());
            return;
        }

        sendSms(phone, message, "PAYMENT_SUCCESS");

    }

    /**
     * Payment receive hone par receiver ko SMS bhejo
     */
    @Async("notificationExecutor")
    @Retryable(retryFor = Exception.class, maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendPaymentReceiveSms(NotificationDto dto){
        String message = buildPaymentReceivedMessage(dto);
        String phone = dto.getSenderPhone();

        if(phone == null || phone.isBlank()){
            log.warn("No phone number for receiverId={}, skipping SMS", dto.getReceiverId());
            return;
        }

        sendSms(phone, message, "PAYMENT_RECEIVE");
    }

    // ================================================================
    // 2. PAYMENT FAILED SMS
    // ================================================================

    @Async("notificationExecutor")
    @Retryable(retryFor = Exception.class, maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendPaymentFailedSms(NotificationDto dto) {
        String message = buildPaymentFailedMessage(dto);
        String phone = dto.getSenderPhone();

        if (phone == null || phone.isBlank()) return;
        sendSms(phone, message, "PAYMENT_FAILED");
    }

    // ================================================================
    // 3. OFFLINE SYNC SUCCESS SMS
    // ================================================================

    @Async("notificationExecutor")
    public void sendSyncSuccessSms(NotificationDto dto) {
        String message = String.format(
                "UPI Mesh: Your offline payment of Rs.%s to %s has been synced successfully. Ref: %s",
                formatAmount(dto.getAmount()), dto.getReceiverUpiId(), dto.getPaymentId()
        );
        if (dto.getSenderPhone() != null) {
            sendSms(dto.getSenderPhone(), message, "SYNC_SUCCESS");
        }
    }


    // ================================================================
    // CORE SMS SEND METHOD
    // ================================================================

    /**
     * SMS bhejne ka core method.
     * Provider ke hisaab se implementation change hoti hai.
     */
    private void sendSms(String phoneNumber, String message, String notificationType){
        if(!smsEnabled){
            // DEV MODE: Console mein print karo
            log.info("=== SMS MOCK [{}] ===", notificationType);
            log.info("To: +91{}", phoneNumber);
            log.info("Message: {}", message);
            log.info("===================");
            return;
        }

        // PRODUCTION: Provider ke hisaab se call karo
        switch (smsProvider.toUpperCase()) {
            case "MSG91"  -> sendViaMSG91(phoneNumber, message);
            case "TWILIO" -> sendViaTwilio(phoneNumber, message);
            case "AWS_SNS"-> sendViaAwsSns(phoneNumber, message);
            default       -> log.warn("Unknown SMS provider: {}. SMS not sent.", smsProvider);
        }
    }

    // ================================================================
    // SMS PROVIDER IMPLEMENTATIONS
    // ================================================================

    /**
     * MSG91 se SMS bhejo (India ka popular provider)
     * API Docs: https://docs.msg91.com/reference/send-sms
     */
    public void sendViaMSG91(String phoneNumber, String message){
        try {
            // MSG91 REST API call
            // POST https://api.msg91.com/api/v5/flow/
            // TODO: RestTemplate ya WebClient se implement karo
            // Example:
            // Map<String, Object> body = new HashMap<>();
            // body.put("template_id", "your_template_id");
            // body.put("short_url", "1");
            // body.put("recipients", List.of(Map.of("mobiles", "91" + phoneNumber)));
            //
            // HttpHeaders headers = new HttpHeaders();
            // headers.set("authkey", msg91AuthKey);
            // headers.setContentType(MediaType.APPLICATION_JSON);
            //
            // restTemplate.postForObject(
            //     "https://api.msg91.com/api/v5/flow/",
            //     new HttpEntity<>(body, headers),
            //     String.class);

            log.info("MSG91 SMS sent to: +91{}", phoneNumber);

        } catch (Exception e) {
            log.error("MSG91 SMS failed to +91{}: {}", phoneNumber, e.getMessage());
            throw new RuntimeException("MSG91 SMS failed", e);
        }
    }

    /**
     * Twilio se SMS bhejo (International)
     */
    private void sendViaTwilio(String phoneNumber, String message) {
        // TODO: Twilio SDK implement karo
        // Message.creator(
        //     new PhoneNumber("+91" + phoneNumber),
        //     new PhoneNumber(twilioFromNumber),
        //     message).create();
        log.info("Twilio SMS sent to: +91{}", phoneNumber);
    }

    /**
     * AWS SNS se SMS bhejo
     */
    private void sendViaAwsSns(String phoneNumber, String message) {
        // TODO: AWS SNS SDK implement karo
        // snsClient.publish(PublishRequest.builder()
        //     .phoneNumber("+91" + phoneNumber)
        //     .message(message)
        //     .build());
        log.info("AWS SNS SMS sent to: +91{}", phoneNumber);
    }




    private String buildPaymentSuccessMessage(NotificationDto dto) {
        return String.format(
                "UPI Mesh: Rs.%s paid to %s successfully. PayID: %s. -UPI Mesh",
                formatAmount(dto.getAmount()),
                dto.getReceiverUpiId(),
                dto.getPaymentId().substring(dto.getPaymentId().length() - 8)
        );
    }

    private String buildPaymentReceivedMessage(NotificationDto dto) {
        return String.format(
                "UPI Mesh: Rs.%s received from %s. PayID: %s. Chk balance on app. -UPI Mesh",
                formatAmount(dto.getAmount()),
                dto.getSenderUpiId(),
                dto.getPaymentId().substring(dto.getPaymentId().length() - 8)
        );
    }

    private String buildPaymentFailedMessage(NotificationDto dto) {
        return String.format(
                "UPI Mesh: FAILED - Rs.%s to %s could NOT be processed. No deduction. Retry on app. -UPI Mesh",
                formatAmount(dto.getAmount()),
                dto.getReceiverUpiId()
        );
    }

    private String formatAmount(java.math.BigDecimal amount) {
        if (amount == null) return "0";
        return amount.stripTrailingZeros().toPlainString();
    }

}
