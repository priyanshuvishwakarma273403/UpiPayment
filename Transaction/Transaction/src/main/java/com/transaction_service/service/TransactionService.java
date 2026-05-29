package com.transaction_service.service;

import com.transaction_service.dto.response.TransactionResponse;
import com.transaction_service.entity.mongo.AuditLog;
import com.transaction_service.entity.mongo.PaymentLog;
import com.transaction_service.entity.mysql.Transaction;
import com.transaction_service.repository.mongo.AuditLogRepository;
import com.transaction_service.repository.mongo.PaymentLogRepository;
import com.transaction_service.repository.mysql.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void recordCompletedPayment(Map<String, Object> event) {

        String paymentId = (String) event.get("paymentId");

        // Duplicate check
        List<Transaction> existing = transactionRepository.findByPaymentId(paymentId);

        if (!existing.isEmpty()) {
            log.warn("Transactions already recorded for paymentId={}", paymentId);
            return;
        }

        Long senderId = Long.valueOf(event.get("senderId").toString());
        Long receiverId = Long.valueOf(event.get("receiverId").toString());

        BigDecimal amount = new BigDecimal(event.get("amount").toString());

        String mode = event.getOrDefault("paymentMode", "UPI")
                .toString()
                .toUpperCase();

        String desc = event.getOrDefault("description", "UPI Payment")
                .toString();

        String refNumber = "REF" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase();

        // Sender DEBIT entry
        Transaction debit = Transaction.builder()
                .paymentId(paymentId)
                .userId(senderId)
                .counterPartyId(receiverId)
                .counterPartyUpiId((String) event.get("receiverUpiId"))
                .amount(amount)
                .transactionType(Transaction.TransactionType.DEBIT)
                .paymentMode(Transaction.PaymentMode.valueOf(mode))
                .status(Transaction.TransactionStatus.SUCCESS)
                .description(desc)
                .referenceNumber(refNumber + "-D")
                .build();

        // Receiver CREDIT entry
        Transaction credit = Transaction.builder()
                .paymentId(paymentId)
                .userId(receiverId)
                .counterPartyId(senderId)
                .counterPartyUpiId((String) event.get("senderUpiId"))
                .amount(amount)
                .transactionType(Transaction.TransactionType.CREDIT)
                .paymentMode(Transaction.PaymentMode.valueOf(mode))
                .status(Transaction.TransactionStatus.SUCCESS)
                .description(desc)
                .referenceNumber(refNumber + "-C")
                .build();

        transactionRepository.save(debit);
        transactionRepository.save(credit);

        log.info("Ledger entries created for paymentId={}", paymentId);

        savePaymentLog(event);

        saveAuditLog(
                paymentId,
                senderId,
                "PAYMENT_COMPLETED",
                "PAYMENT",
                paymentId
        );
    }

    @Transactional
    public void recordFailedPayment(Map<String, Object> event) {

        String paymentId = (String) event.get("paymentId");

        // Duplicate check
        List<Transaction> existing = transactionRepository.findByPaymentId(paymentId);

        if (!existing.isEmpty()) {
            log.warn("Failed transaction already exists for paymentId={}", paymentId);
            return;
        }

        Long senderId = Long.valueOf(event.get("senderId").toString());

        BigDecimal amount = new BigDecimal(event.get("amount").toString());

        String mode = event.getOrDefault("paymentMode", "UPI")
                .toString()
                .toUpperCase();

        Transaction failed = Transaction.builder()
                .paymentId(paymentId)
                .userId(senderId)
                .counterPartyUpiId((String) event.get("receiverUpiId"))
                .amount(amount)
                .transactionType(Transaction.TransactionType.DEBIT)
                .paymentMode(Transaction.PaymentMode.valueOf(mode))
                .status(Transaction.TransactionStatus.FAILED)
                .description("Payment failed")
                .referenceNumber("FAIL-" + paymentId)
                .build();

        transactionRepository.save(failed);

        savePaymentLog(event);

        saveAuditLog(
                paymentId,
                senderId,
                "PAYMENT_FAILED",
                "PAYMENT",
                paymentId
        );

        log.info("Failed payment recorded: paymentId={}", paymentId);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(
            Long userId,
            Pageable pageable
    ) {

        return transactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(TransactionResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getMerchantTransactions(
            Long merchantId,
            Pageable pageable
    ) {

        return transactionRepository
                .findByMerchantIdOrderByCreatedAtDesc(merchantId, pageable)
                .map(TransactionResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalSpend(Long userId, int days) {

        return transactionRepository.getTotalDebitSince(
                userId,
                LocalDateTime.now().minusDays(days)
        );
    }

    private void savePaymentLog(Map<String, Object> event) {

        try {

            PaymentLog paymentLog = PaymentLog.builder()
                    .paymentId((String) event.get("paymentId"))
                    .senderId(Long.valueOf(event.get("senderId").toString()))
                    .receiverId(
                            Long.valueOf(
                                    event.getOrDefault("receiverId", -1).toString()
                            )
                    )
                    .senderUpiId((String) event.get("senderUpiId"))
                    .receiverUpiId((String) event.get("receiverUpiId"))
                    .amount(new BigDecimal(event.get("amount").toString()))
                    .paymentStatus(
                            event.getOrDefault("paymentStatus", "UNKNOWN").toString()
                    )
                    .paymentMode(
                            event.getOrDefault("paymentMode", "UPI").toString()
                    )
                    .paymentTimestamp(LocalDateTime.now())
                    .loggedAt(LocalDateTime.now())
                    .build();

            paymentLogRepository.save(paymentLog);

        } catch (Exception e) {

            log.error("Failed to save payment log: {}", e.getMessage());
        }
    }

    private void saveAuditLog(
            String entityId,
            Long userId,
            String action,
            String entityType,
            String details
    ) {

        try {

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .serviceName("transaction-service")
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

        } catch (Exception e) {

            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }
}