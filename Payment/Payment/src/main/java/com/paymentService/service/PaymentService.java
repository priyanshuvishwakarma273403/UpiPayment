package com.paymentService.service;

import com.paymentService.dto.request.PaymentRequest;
import com.paymentService.dto.response.PaymentResponse;
import com.paymentService.entity.Payment;
import com.paymentService.exception.PaymentException;
import com.paymentService.feign.WalletServiceClient;
import com.paymentService.kafka.PaymentEvent;
import com.paymentService.kafka.PaymentKafkaProducer;
import com.paymentService.repository.PaymentRepository;
import com.paymentService.util.PaymentSignatureUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * ================================================================
 * Payment Service - Complete Business Logic
 * ================================================================
 * Flow for online payment:
 * 1. Idempotency check (duplicate request nahi)
 * 2. Wallet balance validate (Feign -> wallet-service)
 * 3. Payment ID + RSA Signature generate
 * 4. MySQL mein INITIATED status save
 * 5. Kafka: payment_initiated publish (fraud + wallet async process)
 *
 * Flow for offline payment:
 * 1. PENDING_SYNC status se save
 * 2. Sync-service baad mein process karega
 *
 * Circuit Breaker:
 * wallet-service down -> offline queue mein daal do
 * ================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentKafkaProducer kafkaProducer;
    private final PaymentSignatureUtil signatureUtil;
    private final WalletServiceClient walletServiceClient;

    @Transactional
    @CircuitBreaker(name = "walletService", fallbackMethod = "initiatePaymentFallback")
    @Retry(name = "walletService")
    public PaymentResponse initiatePayment(PaymentRequest request, Long senderId) {
        log.info("Initiating payment: sender={}, receiver={}, amount={}",
                senderId, request.getReceiverUpiId(), request.getAmount());

        String idempotencyKey = buildIdempotencyKey(senderId, request);
        if (paymentRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.warn("Duplicate payment detected: {}", idempotencyKey);
            return paymentRepository.findByIdempotencyKey(idempotencyKey)
                    .map(PaymentResponse::fromPayment).orElseThrow();
        }

        validateSenderBalance(senderId, request.getAmount());

        String paymentId = generatePaymentId();
        String signature = signatureUtil.sign(buildSignaturePayload(paymentId, senderId, request));

        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .senderId(senderId)
                .receiverId(request.getReceiverId() != null ? request.getReceiverId() : -1L)
                .senderUpiId(request.getSenderUpiId())
                .receiverUpiId(request.getReceiverUpiId())
                .amount(request.getAmount())
                .paymentStatus(Payment.PaymentStatus.INITIATED)
                .paymentMode(Payment.PaymentMode.UPI)
                .description(request.getDescription())
                .signature(signature)
                .idempotencyKey(idempotencyKey)
                .fraudStatus(Payment.FraudStatus.SAFE)
                .build();

        payment = paymentRepository.save(payment);
        kafkaProducer.publishPaymentInitiated(PaymentEvent.fromPayment(payment));
        log.info("Payment initiated successfully: {}", paymentId);
        return PaymentResponse.fromPayment(payment);
    }

    /** Fallback: wallet-service unreachable -> offline queue */
    public PaymentResponse initiatePaymentFallback(PaymentRequest request, Long senderId, Throwable ex) {
        log.warn("Circuit breaker open. Queuing as offline. Cause: {}", ex.getMessage());
        return initiateOfflinePayment(request, senderId);
    }

    @Transactional
    public PaymentResponse initiateOfflinePayment(PaymentRequest request, Long senderId) {
        log.info("Queuing offline payment: sender={}, amount={}", senderId, request.getAmount());

        String idempotencyKey = buildIdempotencyKey(senderId, request);
        if (paymentRepository.existsByIdempotencyKey(idempotencyKey)) {
            return paymentRepository.findByIdempotencyKey(idempotencyKey)
                    .map(PaymentResponse::fromPayment).orElseThrow();
        }

        String paymentId = generateOfflinePaymentId();
        String signature = signatureUtil.sign(buildSignaturePayload(paymentId, senderId, request));

        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .senderId(senderId)
                .receiverId(request.getReceiverId() != null ? request.getReceiverId() : -1L)
                .senderUpiId(request.getSenderUpiId())
                .receiverUpiId(request.getReceiverUpiId())
                .amount(request.getAmount())
                .paymentStatus(Payment.PaymentStatus.PENDING_SYNC)
                .paymentMode(Payment.PaymentMode.OFFLINE)
                .description(request.getDescription())
                .signature(signature)
                .queuedAt(LocalDateTime.now())
                .idempotencyKey(idempotencyKey)
                .fraudStatus(Payment.FraudStatus.SAFE)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Offline payment queued: {}", paymentId);
        return PaymentResponse.fromPayment(payment);
    }

    public boolean verifyPayment(String paymentId, String providedSignature) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId, HttpStatus.NOT_FOUND));
        boolean valid = payment.getSignature() != null && payment.getSignature().equals(providedSignature);
        log.info("Signature verification paymentId={}: {}", paymentId, valid ? "VALID" : "INVALID");
        return valid;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(String paymentId, Long requestingUserId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId, HttpStatus.NOT_FOUND));
        if (!payment.getSenderId().equals(requestingUserId) && !payment.getReceiverId().equals(requestingUserId)) {
            throw new PaymentException("Access denied to this payment", HttpStatus.FORBIDDEN);
        }
        return PaymentResponse.fromPayment(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentHistory(Long userId, Pageable pageable) {
        return paymentRepository.findBySenderIdOrderByCreatedAtDesc(userId, pageable)
                .map(PaymentResponse::fromPayment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPendingSyncPayments(Long userId) {
        return paymentRepository.findPendingSyncPaymentsBySender(userId)
                .stream().map(PaymentResponse::fromPayment).collect(Collectors.toList());
    }

    // ---- Private Helpers ----

    @SuppressWarnings("unchecked")
    private void validateSenderBalance(Long senderId, BigDecimal amount) {
        try {
            java.util.Map<String, Object> walletResp = walletServiceClient.getBalance(senderId);
            Object bal = walletResp.get("availableBalance");
            if (bal == null) throw new PaymentException("Unable to fetch wallet balance");
            BigDecimal available = new BigDecimal(bal.toString());
            if (available.compareTo(amount) < 0) {
                throw new PaymentException(
                        String.format("Insufficient balance. Available: ₹%s, Required: ₹%s", available, amount),
                        "INSUFFICIENT_BALANCE", HttpStatus.UNPROCESSABLE_ENTITY);
            }
        } catch (PaymentException pe) {
            throw pe;
        } catch (Exception e) {
            log.warn("Balance validation failed (wallet-service error): {}", e.getMessage());
            throw new PaymentException("Wallet service temporarily unavailable");
        }
    }

    private String generatePaymentId() {
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private String generateOfflinePaymentId() {
        return "PAY-OFL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String buildIdempotencyKey(Long senderId, PaymentRequest request) {
        if (request.getClientIdempotencyKey() != null && !request.getClientIdempotencyKey().isEmpty()) {
            return request.getClientIdempotencyKey();
        }
        LocalDateTime now = LocalDateTime.now();
        return String.format("IDM-%d-%s-%s-%d%02d",
                senderId, request.getReceiverUpiId(),
                request.getAmount().toPlainString(), now.getHour(), now.getMinute());
    }

    private String buildSignaturePayload(String paymentId, Long senderId, PaymentRequest request) {
        return String.join("|", paymentId, senderId.toString(),
                request.getReceiverUpiId(), request.getAmount().toPlainString(),
                String.valueOf(System.currentTimeMillis()));
    }

}
