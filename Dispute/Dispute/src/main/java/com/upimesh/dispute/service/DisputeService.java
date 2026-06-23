package com.upimesh.dispute.service;

import com.upimesh.dispute.exception.DisputeNotFoundException;
import com.upimesh.dispute.feign.NotificationServiceClient;
import com.upimesh.dispute.feign.NpciServiceClient;
import com.upimesh.dispute.feign.dto.*;
import com.upimesh.dispute.model.entity.Dispute;
import com.upimesh.dispute.model.entity.DisputeEvidence;
import com.upimesh.dispute.model.enums.DisputeStatus;
import com.upimesh.dispute.model.request.MerchantResponseRequest;
import com.upimesh.dispute.model.request.RaiseDisputeRequest;
import com.upimesh.dispute.model.request.ResolveDisputeRequest;
import com.upimesh.dispute.model.response.DisputeResponse;
import com.upimesh.dispute.repository.DisputeEvidenceRepository;
import com.upimesh.dispute.repository.DisputeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final DisputeEvidenceRepository evidenceRepository;
    private final NpciServiceClient npciServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    @Value("${dispute.internal-key:internal-secret-change-in-prod}")
    private String internalKey;

    /**
     * Raises a transaction dispute.
     * Validates transaction history via NPCI, verifies ownership, and asserts the 90-day raising window.
     */
    @Transactional
    public DisputeResponse raiseDispute(RaiseDisputeRequest request) {
        log.info("Raising dispute for transactionId: {} by user: {}", request.getTransactionId(), request.getUserId());

        if (disputeRepository.findByTransactionId(request.getTransactionId()).isPresent()) {
            throw new IllegalStateException("Dispute already exists for transaction: " + request.getTransactionId());
        }

        NpciApiResponse<TransactionResponse> txnResponse;
        try {
            txnResponse = npciServiceClient.checkStatus(internalKey, request.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to query transaction status from NPCI: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid transaction ID or transaction status inquiry failed");
        }

        if (txnResponse == null || !txnResponse.isSuccess() || txnResponse.getData() == null) {
            String errorMsg = txnResponse != null ? txnResponse.getMessage() : "Null response from NPCI";
            throw new IllegalArgumentException("Transaction validation failed: " + errorMsg);
        }

        TransactionResponse txn = txnResponse.getData();

        if (txn.getStatus() != TransactionStatus.SUCCESS) {
            throw new IllegalStateException("Disputes can only be raised for successful transactions");
        }

        if (!txn.getSenderUpiId().equals(request.getUserUpiId())) {
            throw new IllegalArgumentException("Transaction does not belong to the requesting user");
        }

        if (txn.getCreatedAt().isBefore(LocalDateTime.now().minusDays(90))) {
            throw new IllegalArgumentException("Disputes must be raised within 90 days of the transaction date");
        }

        String disputeId = "DSP" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 15).toUpperCase();

        LocalDateTime raisedAt = LocalDateTime.now();
        LocalDateTime deadline = raisedAt.plusHours(48);

        Dispute dispute = Dispute.builder()
                .disputeId(disputeId)
                .userId(request.getUserId())
                .transactionId(request.getTransactionId())
                .userUpiId(request.getUserUpiId())
                .merchantUpiId(txn.getReceiverUpiId())
                .amount(txn.getAmount())
                .reason(request.getReason())
                .status(DisputeStatus.MERCHANT_NOTIFIED)
                .userDescription(request.getDescription())
                .evidenceUrls(new ArrayList<>())
                .raisedAt(raisedAt)
                .merchantResponseDeadline(deadline)
                .build();

        dispute = disputeRepository.save(dispute);
        log.info("Dispute created successfully with ID: {} and deadline: {}", disputeId, deadline);

        // Notify merchant via Feign notification client
        try {
            NotificationServiceClient.PaymentNotificationRequest notifyReq = 
                    new NotificationServiceClient.PaymentNotificationRequest(
                            txn.getTransactionId(),
                            txn.getSenderUpiId(),
                            txn.getReceiverUpiId(),
                            txn.getAmount(),
                            "DISPUTE_RAISED",
                            "Dispute raised: " + request.getDescription(),
                            null
                    );
            notificationServiceClient.sendPaymentNotification(internalKey, notifyReq);
        } catch (Exception e) {
            log.error("Failed to dispatch merchant dispute alert: {}", e.getMessage());
        }

        return mapToDisputeResponse(dispute);
    }

    /**
     * Submits a merchant response/evidence.
     */
    @Transactional
    public DisputeResponse submitMerchantResponse(String disputeId, MerchantResponseRequest request) {
        log.info("Submitting merchant response for dispute: {}", disputeId);
        Dispute dispute = disputeRepository.findByDisputeId(disputeId)
                .orElseThrow(() -> new DisputeNotFoundException("Dispute not found with ID: " + disputeId));

        if (dispute.getStatus() != DisputeStatus.MERCHANT_NOTIFIED) {
            throw new IllegalStateException("Dispute is not awaiting merchant response. Status: " + dispute.getStatus());
        }

        if (!dispute.getMerchantUpiId().equals(request.getMerchantUpiId())) {
            throw new IllegalArgumentException("Merchant UPI ID does not match the receiver of this transaction");
        }

        dispute.setMerchantResponse(request.getResponse());
        dispute.setStatus(DisputeStatus.UNDER_REVIEW);

        if (request.getEvidenceUrls() != null && !request.getEvidenceUrls().isEmpty()) {
            for (String url : request.getEvidenceUrls()) {
                dispute.getEvidenceUrls().add(url);

                String evidenceId = "EVD" + UUID.randomUUID().toString().replace("-", "")
                        .substring(0, 15).toUpperCase();

                DisputeEvidence evidence = DisputeEvidence.builder()
                        .evidenceId(evidenceId)
                        .disputeId(disputeId)
                        .submittedBy("MERCHANT")
                        .evidenceType("SCREENSHOT")
                        .description("Merchant proof submission")
                        .fileUrl(url)
                        .build();

                evidenceRepository.save(evidence);
            }
        }

        dispute = disputeRepository.save(dispute);
        log.info("Dispute {} successfully transitioned to UNDER_REVIEW", disputeId);

        return mapToDisputeResponse(dispute);
    }

    /**
     * Resolves a dispute.
     * If resolved in favor of USER, initiates refund transaction on NPCI.
     */
    @Transactional
    public DisputeResponse resolveDispute(String disputeId, ResolveDisputeRequest request) {
        log.info("Resolving dispute: {} | favor: {} | notes: {}", disputeId, request.getInFavorOf(), request.getNotes());
        Dispute dispute = disputeRepository.findByDisputeId(disputeId)
                .orElseThrow(() -> new DisputeNotFoundException("Dispute not found with ID: " + disputeId));

        if (dispute.getStatus() == DisputeStatus.RESOLVED_REFUND || dispute.getStatus() == DisputeStatus.RESOLVED_DISMISSED) {
            throw new IllegalStateException("Dispute is already resolved");
        }

        if ("USER".equalsIgnoreCase(request.getInFavorOf())) {
            RefundRequest refundReq = RefundRequest.builder()
                    .originalTransactionId(dispute.getTransactionId())
                    .refundAmount(dispute.getAmount())
                    .reason("DISPUTE_RESOLVED_FAVOR_USER")
                    .initiatedBy("ADMIN")
                    .build();

            try {
                NpciApiResponse<RefundResponse> refundResp = npciServiceClient.initiateRefund(internalKey, refundReq);
                if (refundResp != null && refundResp.isSuccess() && refundResp.getData() != null) {
                    dispute.setStatus(DisputeStatus.RESOLVED_REFUND);
                    log.info("Refund initiated successfully for dispute: {}", disputeId);
                } else {
                    String error = refundResp != null ? refundResp.getMessage() : "Unknown NPCI refund failure";
                    throw new RuntimeException("NPCI refund execution failed: " + error);
                }
            } catch (Exception e) {
                log.error("NPCI refund call failed for dispute: {}", disputeId, e);
                throw new RuntimeException("NPCI refund execution failed: " + e.getMessage());
            }
        } else {
            dispute.setStatus(DisputeStatus.RESOLVED_DISMISSED);
        }

        dispute.setResolvedInFavorOf(request.getInFavorOf());
        dispute.setResolutionNotes(request.getNotes() + " (Resolved by " + request.getResolvedBy() + ")");
        dispute.setResolvedAt(LocalDateTime.now());

        dispute = disputeRepository.save(dispute);
        log.info("Dispute {} resolved.", disputeId);

        // Notify user and merchant about resolution
        try {
            NotificationServiceClient.PaymentNotificationRequest notifyReq = 
                    new NotificationServiceClient.PaymentNotificationRequest(
                            dispute.getTransactionId(),
                            dispute.getUserUpiId(),
                            dispute.getMerchantUpiId(),
                            dispute.getAmount(),
                            "DISPUTE_RESOLVED",
                            "Dispute resolved in favor of " + request.getInFavorOf() + ". Notes: " + request.getNotes(),
                            null
                    );
            notificationServiceClient.sendPaymentNotification(internalKey, notifyReq);
        } catch (Exception e) {
            log.error("Failed to send dispute resolution alerts: {}", e.getMessage());
        }

        return mapToDisputeResponse(dispute);
    }

    /**
     * Scheduled task checking for expired merchant response deadlines every hour.
     * Auto-resolves past-deadline cases in user's favor.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void checkExpiredMerchantResponses() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Scanning for merchant dispute response deadlines expired before: {}", now);

        List<Dispute> expiredDisputes = disputeRepository
                .findByStatusAndMerchantResponseDeadlineLessThanEqual(DisputeStatus.MERCHANT_NOTIFIED, now);

        log.info("Found {} disputes with expired response windows", expiredDisputes.size());

        for (Dispute dispute : expiredDisputes) {
            log.warn("Auto-resolving dispute: {} in favor of user due to deadline expiry", dispute.getDisputeId());

            RefundRequest refundReq = RefundRequest.builder()
                    .originalTransactionId(dispute.getTransactionId())
                    .refundAmount(dispute.getAmount())
                    .reason("DISPUTE_AUTO_RESOLVED_NO_MERCHANT_RESPONSE")
                    .initiatedBy("SYSTEM")
                    .build();

            try {
                NpciApiResponse<RefundResponse> refundResp = npciServiceClient.initiateRefund(internalKey, refundReq);
                if (refundResp != null && refundResp.isSuccess()) {
                    dispute.setStatus(DisputeStatus.RESOLVED_REFUND);
                    dispute.setResolvedInFavorOf("USER");
                    dispute.setResolutionNotes("Auto-resolved in favor of USER: Merchant failed to respond within 48-hour window.");
                    dispute.setResolvedAt(LocalDateTime.now());
                    disputeRepository.save(dispute);
                    log.info("Auto-refund completed for dispute: {}", dispute.getDisputeId());
                } else {
                    String error = refundResp != null ? refundResp.getMessage() : "Unknown NPCI refund failure";
                    escalateDispute(dispute, "Auto-refund failed: " + error);
                }
            } catch (Exception e) {
                log.error("Exception during auto-refund for dispute: {}", dispute.getDisputeId(), e);
                escalateDispute(dispute, "Auto-refund error: " + e.getMessage());
            }
        }
    }

    private void escalateDispute(Dispute dispute, String reason) {
        dispute.setStatus(DisputeStatus.ESCALATED);
        dispute.setResolutionNotes("System auto-resolution failed. Escalated for manual review. Error: " + reason);
        disputeRepository.save(dispute);
        log.warn("Dispute {} escalated for manual review due to refund execution failure", dispute.getDisputeId());
    }

    public DisputeResponse getDispute(String disputeId) {
        Dispute dispute = disputeRepository.findByDisputeId(disputeId)
                .orElseThrow(() -> new DisputeNotFoundException("Dispute not found with ID: " + disputeId));
        return mapToDisputeResponse(dispute);
    }

    public List<DisputeResponse> getDisputesByUser(String userId) {
        return disputeRepository.findByUserId(userId).stream()
                .map(this::mapToDisputeResponse)
                .collect(Collectors.toList());
    }

    public List<DisputeResponse> getDisputesByMerchant(String merchantUpiId) {
        return disputeRepository.findByMerchantUpiId(merchantUpiId).stream()
                .map(this::mapToDisputeResponse)
                .collect(Collectors.toList());
    }

    private DisputeResponse mapToDisputeResponse(Dispute dispute) {
        return DisputeResponse.builder()
                .disputeId(dispute.getDisputeId())
                .transactionId(dispute.getTransactionId())
                .userUpiId(dispute.getUserUpiId())
                .merchantUpiId(dispute.getMerchantUpiId())
                .amount(dispute.getAmount())
                .reason(dispute.getReason())
                .status(dispute.getStatus())
                .userDescription(dispute.getUserDescription())
                .merchantResponse(dispute.getMerchantResponse())
                .evidenceUrls(dispute.getEvidenceUrls())
                .resolvedInFavorOf(dispute.getResolvedInFavorOf())
                .resolutionNotes(dispute.getResolutionNotes())
                .raisedAt(dispute.getRaisedAt())
                .merchantResponseDeadline(dispute.getMerchantResponseDeadline())
                .resolvedAt(dispute.getResolvedAt())
                .build();
    }
}
