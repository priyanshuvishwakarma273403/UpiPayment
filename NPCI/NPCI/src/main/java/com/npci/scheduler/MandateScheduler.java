package com.npci.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


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


}
