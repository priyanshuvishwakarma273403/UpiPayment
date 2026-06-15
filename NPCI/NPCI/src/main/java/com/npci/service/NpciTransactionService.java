package com.npci.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.npci.feign.FraudServiceClient;
import com.npci.feign.NotificationServiceClient;
import com.npci.feign.WalletServiceClient;
import com.npci.model.entity.UpiTransaction;
import com.npci.model.request.InitiateTransactionRequest;
import com.npci.model.response.TransactionResponse;
import com.npci.repository.UpiTransactionRepository;
import com.npci.util.TransactionUtil;
import jakarta.transaction.InvalidTransactionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private final UpiTransactionRepository      transactionRepo;
    private final NpciClient                    npciClient;
    private final FraudServiceClient            fraudClient;
    private final WalletServiceClient           walletClient;
    private final NotificationServiceClient     notificationClient;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${transaction.limits.per-transaction-max}")
    private BigDecimal perTransactionMax;

    @Value("${transaction.limits.daily-max}")
    private BigDecimal dailyMax;

    @Value("${internal.service-key:internal-secret-change-in-prod}")
    private String internalServiceKey;

    @Transactional
    public TransactionResponse initiateTransaction(InitiateTransactionRequest request) throws InvalidTransactionException {

        // Step 1: Idempotency check
        String idempotencyKey = "txn:idem:" + request.getIdempotencyKey();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(idempotencyKey))) {
            String existingTxnId = redisTemplate.opsForValue().get(idempotencyKey);
            log.info("Duplicate idempotency key | key={} | existingTxn={}", request.getIdempotencyKey(), existingTxnId);
            return transactionRepo.findByTransactionId(existingTxnId)
                    .map(this::mapToResponse)
                    .orElseThrow(() -> new TransactionNotFoundException("Idempotent transaction not found: " + existingTxnId));
        }

        // Step 2: Basic validations
        if (request.getSenderUpiId().equalsIgnoreCase(request.getReceiverUpiId())) {
            throw new InvalidTransactionException("Sender and receiver UPI ID cannot be same");
        }
        if (!TransactionUtil.isValidUpiId(request.getSenderUpiId())) {
            throw new InvalidTransactionException("Invalid sender UPI ID: " + request.getSenderUpiId());
        }
        if (!TransactionUtil.isValidUpiId(request.getReceiverUpiId())) {
            throw new InvalidTransactionException("Invalid receiver UPI ID: " + request.getReceiverUpiId());
        }

        // Step 3: Per-transaction limit
        if (request.getAmount().compareTo(perTransactionMax) > 0) {
            throw new TransactionLimitExceededException(
                    "Amount " + TransactionUtil.formatAmount(request.getAmount()) +
                            " exceeds limit of " + TransactionUtil.formatAmount(perTransactionMax));
        }

        // Step 4: Daily limit
        LocalDateTime dayStart = LocalDateTime.now().with(LocalTime.MIDNIGHT);
        BigDecimal todayTotal = transactionRepo.getDailyTotal(request.getSenderUpiId(), dayStart);
        if (todayTotal.add(request.getAmount()).compareTo(dailyMax) > 0) {
            throw new DailyLimitExceededException(
                    "Daily limit of " + TransactionUtil.formatAmount(dailyMax) + " reached. Used: " + TransactionUtil.formatAmount(todayTotal));
        }

        // Step 5: Recent duplicate check
        transactionRepo.findRecentDuplicate(
                        request.getSenderUpiId(), request.getReceiverUpiId(),
                        request.getAmount(), LocalDateTime.now().minusMinutes(5))
                .ifPresent(dup -> { throw new DuplicateTransactionException(
                        "Possible duplicate. Existing: " + dup.getTransactionId()); });

        // Step 6: Fraud check
        String txnId = TransactionUtil.generateTransactionId();
        try {
            FraudServiceClient.FraudCheckResponse fraud = fraudClient.checkTransaction(
                    internalServiceKey,
                    new FraudServiceClient.FraudCheckRequest(txnId, request.getSenderUpiId(),
                            request.getReceiverUpiId(), request.getAmount(),
                            request.getDeviceId(), request.getIpAddress(), request.getType().name()));
            if (!fraud.allowed()) {
                throw new TransactionBlockedException("Transaction blocked: " + fraud.blockReason());
            }
        } catch (TransactionBlockedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Fraud check failed — proceeding | txnId={} | error={}", txnId, e.getMessage());
        }

        // Step 7: Save INITIATED
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
        redisTemplate.opsForValue().set(idempotencyKey, txnId, 10, TimeUnit.MINUTES);

        log.info("Transaction INITIATED | txnId={} | {}->>{} | {}", txnId,
                request.getSenderUpiId(), request.getReceiverUpiId(),
                TransactionUtil.formatAmount(request.getAmount()));

        // Step 8: PENDING + NPCI call
        transaction.setStatus(TransactionStatus.PENDING);
        transactionRepo.save(transaction);

        try {
            JsonNode npciResponse = npciClient.initiateTransaction(
                    request.getSenderUpiId(), request.getReceiverUpiId(),
                    request.getAmount(), txnId, request.getRemarks(), request.getMpinHash());

            TransactionResponse result = processNpciResponse(transaction, npciResponse);

            if (transaction.getStatus() == TransactionStatus.SUCCESS) {
                notifyWalletService(transaction);
            }
            sendPaymentNotification(transaction);
            return result;

        } catch (Exception e) {
            log.error("NPCI call failed | txnId={} | error={}", txnId, e.getMessage());
            transaction.setStatus(TransactionStatus.TIMEOUT);
            transaction.setNpciResponseMessage(e.getMessage());
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepo.save(transaction);
            sendPaymentNotification(transaction);
            throw new NpciCommunicationException("Payment failed. TxnId: " + txnId);
        }
    }

    @Transactional
    public TransactionResponse checkStatus(String transactionId) {
        UpiTransaction transaction = transactionRepo.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));

        if (isTerminalStatus(transaction.getStatus())) return mapToResponse(transaction);

        if (transaction.getNpciTransactionId() != null) {
            try {
                JsonNode npciResponse = npciClient.checkTransactionStatus(transaction.getNpciTransactionId());
                return processNpciResponse(transaction, npciResponse);
            } catch (Exception e) {
                log.warn("NPCI poll failed | txnId={}", transactionId);
            }
        }
        return mapToResponse(transaction);
    }

    private TransactionResponse processNpciResponse(UpiTransaction txn, JsonNode resp) {
        if (resp == null) {
            txn.setStatus(TransactionStatus.TIMEOUT);
            txn.setCompletedAt(LocalDateTime.now());
            transactionRepo.save(txn);
            return mapToResponse(txn);
        }
        String code    = resp.path("responseCode").asText("");
        String status  = resp.path("status").asText("FAILURE");
        String message = TransactionUtil.getNpciResponseMessage(code);

        txn.setNpciTransactionId(resp.path("txnId").asText(null));
        txn.setRrn(resp.path("rrn").asText(null));
        txn.setNpciResponseCode(code);
        txn.setNpciResponseMessage(message);
        txn.setNpciResponseAt(LocalDateTime.now());

        if ("SUCCESS".equalsIgnoreCase(status) || "00".equals(code)) {
            txn.setStatus(TransactionStatus.SUCCESS);
            log.info("Payment SUCCESS | txnId={} | rrn={}", txn.getTransactionId(), txn.getRrn());
        } else if ("Z9".equals(code) || "51".equals(code)) {
            txn.setStatus(TransactionStatus.DECLINED);
        } else {
            txn.setStatus(TransactionStatus.FAILED);
        }
        txn.setCompletedAt(LocalDateTime.now());
        transactionRepo.save(txn);
        return mapToResponse(txn);
    }

    private void notifyWalletService(UpiTransaction txn) {
        try {
            walletClient.debitWallet(internalServiceKey,
                    new WalletServiceClient.WalletDebitRequest(txn.getTransactionId(),
                            txn.getSenderUpiId(), txn.getAmount(), txn.getRemarks()));
            walletClient.creditWallet(internalServiceKey,
                    new WalletServiceClient.WalletCreditRequest(txn.getTransactionId(),
                            txn.getReceiverUpiId(), txn.getAmount(), txn.getRemarks()));
        } catch (Exception e) {
            log.error("CRITICAL: Wallet update failed! txnId={} | RECONCILIATION NEEDED | error={}",
                    txn.getTransactionId(), e.getMessage());
        }
    }

    private void sendPaymentNotification(UpiTransaction txn) {
        try {
            notificationClient.sendPaymentNotification(internalServiceKey,
                    new NotificationServiceClient.PaymentNotificationRequest(
                            txn.getTransactionId(), txn.getSenderUpiId(), txn.getReceiverUpiId(),
                            txn.getAmount(), txn.getStatus().name(), txn.getRemarks(), txn.getNpciResponseCode()));
        } catch (Exception e) {
            log.warn("Notification failed | txnId={}", txn.getTransactionId());
        }
    }

    private boolean isTerminalStatus(TransactionStatus status) {
        return status == TransactionStatus.SUCCESS || status == TransactionStatus.FAILED
                || status == TransactionStatus.DECLINED || status == TransactionStatus.REVERSED;
    }

    public TransactionResponse mapToResponse(UpiTransaction t) {
        return TransactionResponse.builder()
                .transactionId(t.getTransactionId()).npciTransactionId(t.getNpciTransactionId())
                .rrn(t.getRrn()).senderUpiId(t.getSenderUpiId()).receiverUpiId(t.getReceiverUpiId())
                .amount(t.getAmount()).status(t.getStatus()).type(t.getType())
                .remarks(t.getRemarks()).npciResponseCode(t.getNpciResponseCode())
                .npciResponseMessage(t.getNpciResponseMessage())
                .createdAt(t.getCreatedAt()).completedAt(t.getCompletedAt()).build();
    }
}