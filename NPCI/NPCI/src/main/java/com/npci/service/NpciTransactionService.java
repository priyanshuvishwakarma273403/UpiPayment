package com.npci.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.npci.model.entity.UpiTransaction;
import com.npci.model.exception.TransactionNotFound;
import com.npci.model.request.InitiateTransactionRequest;
import com.npci.model.response.TransactionResponse;
import com.npci.repository.UpiTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.TransactionalIdNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * NpciTransactionService — Core business logic layer.
 *
 * Flow for a payment:
 * 1. Validate request (limits, duplicates)
 * 2. Save transaction as INITIATED
 * 3. Call NpciClient to send to NPCI
 * 4. Update status based on NPCI response
 * 5. Publish event to Wallet/Notification services
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class NpciTransactionService {

    private final UpiTransactionRepository transactionRepo;
    private final NpciClient npciClient;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${transaction.limits.per-transaction-max}")
    private BigDecimal perTransactionMax;

    @Value("${transaction.limits.daily-max}")
    private BigDecimal dailyMax;

    // ─── Initiate Transaction ─────────────────────────────────────────────────

    @Transactional
    public TransactionResponse initiateTransaction(InitiateTransactionRequest request) {

        // Step 1: Check idempotency (same request sent twice?)
        String idempotencyKey = "txn:idem:" + request.getIdempotencyKey();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(idempotencyKey))) {
            String existingTxnId = redisTemplate.opsForValue().get(idempotencyKey);
            log.info("Duplicate request detected | idempotencyKey={}", request.getIdempotencyKey());
            UpiTransaction existing = transactionRepo.findByTransactionId(existingTxnId)
                    .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
            return mapToResponse(existing);
        }

        // Step 2: Validate UPI IDs are not the same
        if (request.getSenderUpiId().equalsIgnoreCase(request.getReceiverUpiId())) {
            throw new InvalidTransactionException("Sender and receiver cannot be the same UPI ID");
        }

        // Step 3: Check per-transaction limit
        if (request.getAmount().compareTo(perTransactionMax) > 0) {
            throw new TransactionLimitExceededException(
                    "Amount exceeds per-transaction limit of ₹" + perTransactionMax);
        }

        // Step 4: Check daily limit
        LocalDateTime dayStart = LocalDateTime.now().with(LocalTime.MIDNIGHT);
        BigDecimal todayTotal = transactionRepo.getDailyTotal(request.getSenderUpiId(), dayStart);
        if (todayTotal.add(request.getAmount()).compareTo(dailyMax) > 0) {
            throw new DailyLimitExceededException(
                    "Daily UPI limit exceeded. Used: ₹" + todayTotal + " | Limit: ₹" + dailyMax);
        }

        // Step 5: Check for recent duplicate (same amount to same receiver in last 5 min)
        LocalDateTime fiveMinAgo = LocalDateTime.now().minusMinutes(5);
        transactionRepo.findRecentDuplicate(
                        request.getSenderUpiId(), request.getReceiverUpiId(),
                        request.getAmount(), fiveMinAgo)
                .ifPresent(dup -> {
                    throw new DuplicateTransactionException(
                            "Duplicate transaction detected. Existing txnId: " + dup.getTransactionId());
                });

        // Step 6: Create transaction record as INITIATED
        String txnId = "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        UpiTransaction transaction = UpiTransaction.builder()
                .transactionId(txnId)
                .senderUpiId(request.getSenderUpiId())
                .receiverUpiId(request.getReceiverUpiId())
                .amount(request.getAmount())
                .remarks(request.getRemarks())
                .type(request.getType())
                .status(TransactionStatus.INITIATED)
                .deviceId(request.getDeviceId())
                .ipAddress(request.getIpAddress())
                .build();

        transactionRepo.save(transaction);
        log.info("Transaction created | txnId={} | amount={}", txnId, request.getAmount());

        // Step 7: Store idempotency key in Redis for 10 minutes
        redisTemplate.opsForValue().set(idempotencyKey, txnId, 10, TimeUnit.MINUTES);

        // Step 8: Update status to PENDING before NPCI call
        transaction.setStatus(TransactionStatus.PENDING);
        transactionRepo.save(transaction);

        // Step 9: Call NPCI
        try {
            JsonNode npciResponse = npciClient.initiateTransaction(
                    request.getSenderUpiId(),
                    request.getReceiverUpiId(),
                    request.getAmount(),
                    txnId,
                    request.getRemarks(),
                    request.getMpinHash()
            );

            // Step 10: Process NPCI response
            return processNpciResponse(transaction, npciResponse);

        } catch (Exception e) {
            log.error("NPCI call failed | txnId={} | error={}", txnId, e.getMessage());
            transaction.setStatus(TransactionStatus.TIMEOUT);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepo.save(transaction);
            throw new NpciCommunicationException("Payment processing failed. Please retry.");
        }
    }

    // ─── Check Status ─────────────────────────────────────────────────────────

    @Transactional
    public TransactionResponse checkStatus(String transactionId) {
        UpiTransaction transaction = transactionRepo.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionalIdNotFoundException(
                        "Transaction not found: " + transactionId));

        // If already in a terminal state, return as-is
        if (isTerminalStatus(transaction.getStatus())) {
            return mapToResponse(transaction);
        }

        // Otherwise, poll NPCI for latest status
        if (transaction.getNpciTransactionId() != null) {
            try {
                JsonNode npciResponse = npciClient.checkTransactionStatus(
                        transaction.getNpciTransactionId());
                return processNpciResponse(transaction, npciResponse);
            } catch (Exception e) {
                log.warn("NPCI status check failed | txnId={}", transactionId);
            }
        }

        return mapToResponse(transaction);
    }

    // ─── Process NPCI Response ────────────────────────────────────────────────

    private TransactionResponse processNpciResponse(UpiTransaction transaction, JsonNode npciResponse) {
        if (npciResponse == null) {
            transaction.setStatus(TransactionStatus.TIMEOUT);
            transactionRepo.save(transaction);
            return mapToResponse(transaction);
        }

        String responseCode = npciResponse.path("responseCode").asText("");
        String npciTxnId = npciResponse.path("txnId").asText(null);
        String rrn = npciResponse.path("rrn").asText(null);
        String status = npciResponse.path("status").asText("FAILURE");
        String message = npciResponse.path("message").asText("");

        // Update NPCI reference IDs
        transaction.setNpciTransactionId(npciTxnId);
        transaction.setRrn(rrn);
        transaction.setNpciResponseCode(responseCode);
        transaction.setNpciResponseMessage(message);
        transaction.setNpciResponseAt(LocalDateTime.now());

        // NPCI response code "00" = success (standard banking code)
        if ("SUCCESS".equalsIgnoreCase(status) || "00".equals(responseCode)) {
            transaction.setStatus(TransactionStatus.SUCCESS);
            log.info("✅ Payment SUCCESS | txnId={} | rrn={}", transaction.getTransactionId(), rrn);
        } else if ("Z9".equals(responseCode)) {
            // Z9 = Insufficient funds (very common failure reason)
            transaction.setStatus(TransactionStatus.DECLINED);
            log.info("❌ Payment DECLINED (insufficient funds) | txnId={}", transaction.getTransactionId());
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            log.info("❌ Payment FAILED | txnId={} | code={}", transaction.getTransactionId(), responseCode);
        }

        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepo.save(transaction);

        return mapToResponse(transaction);
    }

    // ─── Helper methods ───────────────────────────────────────────────────────

    private boolean isTerminalStatus(TransactionStatus status) {
        return status == TransactionStatus.SUCCESS
                || status == TransactionStatus.FAILED
                || status == TransactionStatus.DECLINED
                || status == TransactionStatus.REVERSED;
    }

    public TransactionResponse mapToResponse(UpiTransaction t) {
        return TransactionResponse.builder()
                .transactionId(t.getTransactionId())
                .npciTransactionId(t.getNpciTransactionId())
                .rrn(t.getRrn())
                .senderUpiId(t.getSenderUpiId())
                .receiverUpiId(t.getReceiverUpiId())
                .amount(t.getAmount())
                .status(t.getStatus())
                .type(t.getType())
                .remarks(t.getRemarks())
                .npciResponseCode(t.getNpciResponseCode())
                .npciResponseMessage(t.getNpciResponseMessage())
                .createdAt(t.getCreatedAt())
                .completedAt(t.getCompletedAt())
                .build();
    }
}
