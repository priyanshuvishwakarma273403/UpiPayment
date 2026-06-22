package com.upimesh.settlement.scheduler;

import com.upimesh.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementScheduler {

    private final SettlementService settlementService;

    @Scheduled(cron = "${settlement.schedule.cron:0 0 1 * * *}")
    public void scheduleDailySettlement() {
        log.info("Triggering scheduled daily settlement batch run...");
        try {
            settlementService.runDailySettlement();
            log.info("Scheduled daily settlement batch run completed successfully.");
        } catch (Exception e) {
            log.error("Error occurred during scheduled daily settlement run", e);
        }
    }

    @Scheduled(fixedDelay = 14400000) // 4 hours in milliseconds
    public void scheduleFailedSettlementRetry() {
        log.info("Triggering scheduled failed settlements retry run...");
        try {
            settlementService.retryFailedSettlements();
            log.info("Scheduled failed settlements retry run completed.");
        } catch (Exception e) {
            log.error("Error occurred during scheduled failed settlements retry run", e);
        }
    }
}
