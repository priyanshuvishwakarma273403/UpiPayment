package com.npci.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.npci.exception.DuplicateTransactionException;
import com.npci.model.entity.RefundRecord;
import com.npci.model.entity.UpiTransaction;
import com.npci.model.enums.RefundStatus;
import com.npci.model.enums.TransactionStatus;
import com.npci.model.request.RefundRequest;
import com.npci.model.response.RefundResponse;
import com.npci.repository.RefundRecordRepository;
import com.npci.repository.UpiTransactionRepository;
import jakarta.transaction.InvalidTransactionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.TransactionalIdNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RefundService — Handles full refund lifecycle.
 *
 * Only SUCCESS transactions can be refunded.
 * Partial refunds are supported (refundAmount <= original amount).
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {
    private final UpiTransactionRepository transactionRepo;
    private final RefundRecordRepository refundRepo;
    private final NpciClient npciClient;

    @Transactional
    public RefundResponse initiateRefund(RefundRequest request) throws InvalidTransactionException {

        // Step 1: Find original transaction
        UpiTransaction original = transactionRepo.findByTransactionId(
                        request.getOriginalTransactionId())
                .orElseThrow(() -> new TransactionalIdNotFoundException(
                        "Original transaction not found: " + request.getOriginalTransactionId()));

        // Step 2: Only SUCCESS transactions can be refunded
        if (original.getStatus() != TransactionStatus.SUCCESS) {
            throw new InvalidTransactionException(
                    "Only successful transactions can be refunded. Current status: " + original.getStatus());
        }

        // Step 3: Check if already refunded
        if (original.getRefundTransactionId() != null) {
            throw new DuplicateTransactionException(
                    "Transaction already refunded. Refund ID: " + original.getRefundTransactionId());
        }

        // Step 4: Validate refund amount (cannot exceed original)
        if (request.getRefundAmount().compareTo(original.getAmount()) > 0) {
            throw new InvalidTransactionException(
                    "Refund amount ₹" + request.getRefundAmount() +
                            " cannot exceed original amount ₹" + original.getAmount());
        }

        // Step 5: Create refund record
        String refundId = "RFND" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        RefundRecord refund = RefundRecord.builder()
                .refundId(refundId)
                .originalTransactionId(request.getOriginalTransactionId())
                .refundAmount(request.getRefundAmount())
                .refundToUpiId(original.getSenderUpiId())   // Money goes back to original sender
                .reason(request.getReason())
                .initiatedBy(request.getInitiatedBy())
                .status(RefundStatus.REQUESTED)
                .expectedCreditBy(LocalDateTime.now().plusDays(3)) // T+3 typical
                .build();

        refundRepo.save(refund);
        log.info("Refund record created | refundId={} | originalTxn={}",
                refundId, request.getOriginalTransactionId());

        // Step 6: Send to NPCI
        try {
            refund.setStatus(RefundStatus.PROCESSING);
            refundRepo.save(refund);

            JsonNode npciResponse = npciClient.initiateRefund(
                    original.getNpciTransactionId(),
                    refundId,
                    request.getRefundAmount(),
                    request.getReason()
            );

            String responseCode = npciResponse.path("responseCode").asText("");
            String status = npciResponse.path("status").asText("FAILURE");
            String npciRefundTxnId = npciResponse.path("txnId").asText(null);

            refund.setNpciRefundTxnId(npciRefundTxnId);
            refund.setNpciResponseCode(responseCode);
            refund.setNpciResponseMessage(npciResponse.path("message").asText(""));

            if ("SUCCESS".equalsIgnoreCase(status) || "00".equals(responseCode)) {
                refund.setStatus(RefundStatus.COMPLETED);
                refund.setCompletedAt(LocalDateTime.now());

                // Mark original transaction as reversed
                original.setStatus(TransactionStatus.REVERSED);
                original.setRefundTransactionId(refundId);
                transactionRepo.save(original);

                log.info("✅ Refund SUCCESS | refundId={}", refundId);
            } else {
                refund.setStatus(RefundStatus.FAILED);
                log.warn("❌ Refund FAILED | refundId={} | code={}", refundId, responseCode);
            }

        } catch (Exception e) {
            log.error("NPCI refund call failed | refundId={} | error={}", refundId, e.getMessage());
            refund.setStatus(RefundStatus.FAILED);
        }

        refundRepo.save(refund);
        return mapToResponse(refund);
    }

    public RefundResponse getRefundStatus(String refundId) {
        RefundRecord refund = refundRepo.findByRefundId(refundId)
                .orElseThrow(() -> new TransactionalIdNotFoundException("Refund not found: " + refundId));
        return mapToResponse(refund);
    }

    private RefundResponse mapToResponse(RefundRecord r) {
        return RefundResponse.builder()
                .refundId(r.getRefundId())
                .originalTransactionId(r.getOriginalTransactionId())
                .refundAmount(r.getRefundAmount())
                .refundToUpiId(r.getRefundToUpiId())
                .status(r.getStatus())
                .reason(r.getReason())
                .expectedCreditBy(r.getExpectedCreditBy())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
