package com.upimesh.reconciliation.scheduler;

import com.upimesh.reconciliation.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReconciliationScheduler {

    private final ReconciliationService reconciliationService;

    @Value("${reconciliation.lookback-days:3}")
    private int lookbackDays;

    @Scheduled(cron = "${reconciliation.schedule.cron:0 30 2 * * *}")
    public void scheduleDailyReconciliation() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Triggering scheduled daily reconciliation run for date: {}", yesterday);
        long startTime = System.currentTimeMillis();
        
        try {
            reconciliationService.runDailyReconciliation(yesterday);
            long duration = System.currentTimeMillis() - startTime;
            log.info("Scheduled daily reconciliation run for date: {} completed. Duration: {} ms", yesterday, duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error occurred during scheduled daily reconciliation run for date: {} | Duration: {} ms", yesterday, duration, e);
        }
    }

    @Scheduled(cron = "0 0 3 * * *") // Run daily at 3 AM
    public void scheduleLookbackReconciliation() {
        log.info("Triggering scheduled lookback catch-up job for last {} days...", lookbackDays);
        long startTime = System.currentTimeMillis();
        
        try {
            LocalDate today = LocalDate.now();
            for (int i = 1; i <= lookbackDays; i++) {
                LocalDate targetDate = today.minusDays(i);
                log.info("Running lookback reconciliation checks for date: {}", targetDate);
                try {
                    reconciliationService.runDailyReconciliation(targetDate);
                } catch (Exception e) {
                    log.error("Catch-up reconciliation failed for date: {}", targetDate, e);
                }
            }
            long duration = System.currentTimeMillis() - startTime;
            log.info("Scheduled lookback catch-up job completed. Duration: {} ms", duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error occurred during scheduled lookback catch-up job | Duration: {} ms", duration, e);
        }
    }
}
