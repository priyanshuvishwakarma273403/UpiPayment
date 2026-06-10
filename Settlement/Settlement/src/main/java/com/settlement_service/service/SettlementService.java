package com.settlement_service.service;

import com.settlement_service.repository.SettlementBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


/**
 * ================================================================
 * Settlement Service - EOD Batch Processing
 * ================================================================
 * EOD (End of Day) process:
 * 1. Saare completed payments of the day aggregate karo per merchant
 * 2. MDR (Merchant Discount Rate) calculate karo
 *    - UPI MDR: 0% (RBI mandate - no MDR on UPI since Jan 2020)
 *    - Card MDR: 0.9% - 1.8% depending on card type
 * 3. GST on MDR (18%)
 * 4. Net amount NEFT/RTGS transfer karo
 * 5. Settlement report generate karo
 *
 * Schedule: Daily at 11 PM IST (EOD)
 * T+1 Settlement (transactions today -> settled tomorrow)
 * ================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final SettlementBatchRepository settlementBatchRepository;

    // UPI MDR = 0% (RBI mandate since January 2020)
    private static final BigDecimal UPI_MDR_RATE = BigDecimal.ZERO;
    private static final BigDecimal GST_RATE     = new BigDecimal("0.18");
    private static final BigDecimal MDR_RATE_CARD = new BigDecimal("0.009"); // 0.9%

    // ================================================================
    // 1. EOD SETTLEMENT BATCH (Scheduler)
    // ================================================================

    /**
     * Daily EOD settlement - 11 PM mein run hoga
     * Saare merchants ke liye batch create karega
     */
    @Scheduled(cron = "0 0 23 *")

}
