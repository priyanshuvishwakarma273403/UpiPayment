package com.notification_service.service;

import com.notification_service.dto.NotificationDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Email Notification Service - Thymeleaf HTML emails
// * @Async -> Dedicated thread pool
// * @Retryable -> SMTP fail hone par 3 baar retry
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    private static final String FROM_EMAIL = "noreply@upimesh.com";
    private static final String FROM_NAME  = "UPI Payment Mesh";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");


    @Async("notificationExecutor")
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendPaymentSuccessToSender(NotificationDto dto){
        if(!isValid(dto.getSenderEmail())) return;
        try{
            Context ctx = buildCtx(dto);
            ctx.setVariable("senderName", dto.getSenderName() != null ? dto.getSenderName() : "User");
            String html = templateEngine.process("payment-success", ctx);
            sendHtml(dto.getSenderEmail(), "Payment of ₹" + dto.getAmount() + " sent successfully", html);
            log.info("Success email sent to sender: {}", dto.getSenderEmail());
        } catch (Exception e) {
            log.error("Send success email failed: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Async("notificationExecutor")
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendPaymentReceiverToReceiver(NotificationDto dto){
        if(!isValid(dto.getReceiverEmail())) return;
        try{
            Context ctx = buildCtx(dto);
            ctx.setVariable("receiverName", dto.getReceiverName() != null ? dto.getReceiverName() : "User");
            String html = templateEngine.process("payment-success", ctx);
            sendHtml(dto.getReceiverEmail(), "You received ₹" + dto.getAmount() + " via UPI Mesh", html);
            log.info("Received email sent to receiver: {}", dto.getReceiverEmail());
        }catch (Exception e){
            log.error("Send received email failed: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Async("notificationExecutor")
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendPaymentFailed(NotificationDto dto) {
        if (!isValid(dto.getSenderEmail())) return;
        try {
            Context ctx = buildCtx(dto);
            ctx.setVariable("senderName",    dto.getSenderName()    != null ? dto.getSenderName()    : "User");
            ctx.setVariable("failureReason", dto.getFailureReason() != null ? dto.getFailureReason() : "Technical issue");
            String html = templateEngine.process("payment-failed", ctx);
            sendHtml(dto.getSenderEmail(), "Payment of ₹" + dto.getAmount() + " FAILED", html);
            log.info("Failed email sent: {}", dto.getSenderEmail());
        } catch (Exception e) {
            log.error("Send failed email error: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Async("notificationExecutor")
    public void sendSyncCompleted(NotificationDto dto) {
        if (!isValid(dto.getSenderEmail())) return;
        try {
            Context ctx = buildCtx(dto);
            String html = templateEngine.process("payment-success", ctx);
            sendHtml(dto.getSenderEmail(), "Offline payment of ₹" + dto.getAmount() + " synced successfully", html);
        } catch (Exception e) {
            log.error("Send sync email error: {}", e.getMessage());
        }
    }

    private void sendHtml(String to, String subject, String html) throws MessagingException, UnsupportedEncodingException {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true,"UTF-8");
        helper.setFrom(FROM_EMAIL, FROM_NAME);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(msg);
    }

    private Context buildCtx(NotificationDto dto){
        Context ctx = new Context() ;
        ctx.setVariable("paymentId",     dto.getPaymentId());
        ctx.setVariable("amount",        dto.getAmount());
        ctx.setVariable("senderUpiId",   dto.getSenderUpiId());
        ctx.setVariable("receiverUpiId", dto.getReceiverUpiId());
        ctx.setVariable("paymentMode",   dto.getPaymentMode() != null ? dto.getPaymentMode() : "UPI");
        ctx.setVariable("description",   dto.getDescription());
        ctx.setVariable("timestamp",     dto.getTimestamp() != null ? dto.getTimestamp().format(FMT) : LocalDateTime.now().format(FMT));
        return ctx;
    }

    private boolean isValid(String email) {
        boolean valid = email != null && !email.isBlank() && email.contains("@");
        if (!valid) log.warn("Invalid/missing email - skipping notification");
        return valid;
    }

}
