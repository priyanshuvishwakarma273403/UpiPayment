package com.syncService.Sync.scheduler;

import com.syncService.Sync.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ================================================================
 * Sync Scheduler - Automatic Offline Payment Sync
 * ================================================================
 * Yeh scheduler automatically har 5 minute mein run hota hai
 * aur saare PENDING_SYNC payments ko process karne ki koshish karta hai.
 *
 * Cron Expressions:
 * "0 *\/5 * * * ?" = Har 5 minute
 * "0 0 * * * ?"   = Har ghante
 *
// * @Scheduled fixedDelay: Previous execution complete hone ke baad wait karo
// * @Scheduled fixedRate:  Fixed interval par run karo (overlap possible)
 * ================================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SyncScheduler {


    private final SyncService syncService;

    /**
     * Har 5 minute mein saare pending payments sync karo
     * initialDelay: Application start hone ke 30 sec baad pehli baar chalega
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 30000)
    public void scheduledSync() {
        log.info("=== Scheduled sync job started ===");
        try {
            // System-level sync: saare pending payments (userId = -1 means all)
            syncService.syncAllPendingPayments();
        } catch (Exception e) {
            log.error("Scheduled sync job failed: {}", e.getMessage(), e);
        }
        log.info("=== Scheduled sync job completed ===");
    }

    /**
     * Har raat 1 AM: Failed syncs retry karo (exponential backoff ke saath)
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void retryFailedSyncs() {
        log.info("=== Retry failed syncs started ===");
        try {
            syncService.retryFailedSyncs();
        } catch (Exception e) {
            log.error("Retry failed syncs job failed: {}", e.getMessage(), e);
        }
        log.info("=== Retry failed syncs completed ===");
    }



}
