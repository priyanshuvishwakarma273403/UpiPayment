package com.npci.scheduler;

import com.npci.model.entity.RefundRecord;
import com.npci.model.entity.UpiMandate;
import com.npci.model.entity.UpiTransaction;
import com.npci.model.enums.MandateStatus;
import com.npci.model.enums.RefundStatus;
import com.npci.model.enums.TransactionStatus;
import com.npci.repository.RefundRecordRepository;
import com.npci.repository.UpiMandateRepository;
import com.npci.repository.UpiTransactionRepository;
import com.npci.service.MandateService;
import com.npci.service.NpciClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


/**
 * MandateScheduler — Runs daily jobs for:
 * 1. Executing due mandates (auto-debit at midnight)
 * 2. Expiring old mandates past end date
 * 3. Retrying failed refunds
 * 4. Timing out stuck PENDING transactions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MandateScheduler {

    private final MandateService mandateService;
    private final UpiMandateRepository mandateRepo;
    private final UpiTransactionRepository transactionRepo;
    private final RefundRecordRepository refundRepo;
    private final NpciClient npciClient;

    /**
     * Run every day at 00:05 AM — execute all mandates due today
     * Cron: second minute hour day month weekday
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void executeDueMandates() {
        log.info("⏰ Mandate execution job started | date={}", LocalDate.now());
        try {
            mandateService.executeDueMandates();
            log.info("✅ Mandate execution job completed");
        } catch (Exception e) {
            log.error("❌ Mandate execution job failed | error={}", e.getMessage());
        }
    }

    /**
     * Run every day at 00:30 AM — expire mandates past end date
     */
    @Scheduled(cron = "0 30 0 * * *")
    public void expireOldMandates() {
        log.info("⏰ Mandate expiry job started");
        List<UpiMandate> expired = mandateRepo.findExpiredMandates(LocalDate.now());
        for (UpiMandate mandate : expired) {
            mandate.setStatus(MandateStatus.EXPIRED);
            mandateRepo.save(mandate);
            log.info("Mandate expired | mandateId={}", mandate.getMandateId());
        }
        log.info("✅ Expired {} mandates", expired.size());
    }

    /**
     * Run every 30 minutes — retry failed refunds (max 3 attempts)
     */
    @Scheduled(fixedDelay = 1800000) // 30 minutes in ms
    public void retryFailedRefunds() {
        List<RefundRecord> failedRefunds = refundRepo.findByStatusAndRetryCountLessThan(
                RefundStatus.FAILED, 3);

        if (!failedRefunds.isEmpty()) {
            log.info("⏰ Retrying {} failed refunds", failedRefunds.size());
        }

        for (RefundRecord refund : failedRefunds) {
            try {
                refund.setRetryCount(refund.getRetryCount() + 1);
                refund.setStatus(RefundStatus.PROCESSING);
                refundRepo.save(refund);

                // Find original txn to get NPCI ID
                transactionRepo.findByTransactionId(refund.getOriginalTransactionId())
                        .ifPresent(original -> {
                            npciClient.initiateRefund(
                                    original.getNpciTransactionId(),
                                    refund.getRefundId(),
                                    refund.getRefundAmount(),
                                    refund.getReason()
                            );
                        });

                refund.setStatus(RefundStatus.COMPLETED);
                refund.setCompletedAt(LocalDateTime.now());
                refundRepo.save(refund);
                log.info("✅ Refund retry succeeded | refundId={}", refund.getRefundId());

            } catch (Exception e) {
                refund.setStatus(RefundStatus.FAILED);
                refundRepo.save(refund);
                log.warn("❌ Refund retry failed | refundId={} | attempt={}",
                        refund.getRefundId(), refund.getRetryCount());
            }
        }
    }

    /**
     * Run every 15 minutes — timeout PENDING transactions older than 10 minutes
     * (NPCI SLA is typically 30 seconds, but we give 10 min buffer)
     */
    @Scheduled(fixedDelay = 900000) // 15 minutes
    public void timeoutStuckTransactions() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<UpiTransaction> stuck = transactionRepo
                .findByStatusAndCreatedAtBefore(TransactionStatus.PENDING, tenMinutesAgo);

        if (!stuck.isEmpty()) {
            log.info("⏰ Timing out {} stuck transactions", stuck.size());
        }

        for (UpiTransaction txn : stuck) {
            txn.setStatus(TransactionStatus.TIMEOUT);
            txn.setCompletedAt(LocalDateTime.now());
            transactionRepo.save(txn);
            log.warn("Transaction timed out | txnId={} | age={}min",
                    txn.getTransactionId(),
                    java.time.Duration.between(txn.getCreatedAt(), LocalDateTime.now()).toMinutes());
        }
    }
}
