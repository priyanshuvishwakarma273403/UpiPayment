package com.upimesh.settlement.service;

import com.upimesh.settlement.feign.TransactionServiceClient.UnsettledTransactionDto;
import com.upimesh.settlement.model.enums.SettlementMode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SettlementCalculationServiceTest {

    private final SettlementCalculationService calculationService = new SettlementCalculationService();

    @Test
    public void platformFeeCalculation() {
        // 0.2% of 1000 = 2.00
        BigDecimal fee1 = calculationService.calculatePlatformFee(BigDecimal.valueOf(1000));
        assertEquals(new BigDecimal("2.00"), fee1);

        // Clamping to MIN_FEE = 1.00
        BigDecimal fee2 = calculationService.calculatePlatformFee(BigDecimal.valueOf(100));
        assertEquals(new BigDecimal("1.00"), fee2);

        // Clamping to MAX_FEE = 1000.00
        BigDecimal fee3 = calculationService.calculatePlatformFee(BigDecimal.valueOf(1000000));
        assertEquals(new BigDecimal("1000.00"), fee3);
    }

    @Test
    public void gstCalculation() {
        // 18% of 10 = 1.80
        BigDecimal gst1 = calculationService.calculateGst(BigDecimal.valueOf(10));
        assertEquals(new BigDecimal("1.80"), gst1);
    }

    @Test
    public void netAmountCalculation() {
        // 1000 - 2 - 0.36 = 997.64
        BigDecimal net = calculationService.calculateNetAmount(
                BigDecimal.valueOf(1000), BigDecimal.valueOf(2), BigDecimal.valueOf(0.36));
        assertEquals(new BigDecimal("997.64"), net);
    }

    @Test
    public void settlementModeForSmallAmount() {
        // < 1000 is IMPS
        SettlementMode mode = calculationService.determineSettlementMode(BigDecimal.valueOf(500));
        assertEquals(SettlementMode.IMPS, mode);
    }

    @Test
    public void settlementModeForLargeAmount() {
        // >= 1000 is NEFT
        SettlementMode mode1 = calculationService.determineSettlementMode(BigDecimal.valueOf(1500));
        assertEquals(SettlementMode.NEFT, mode1);

        // >= 200000 is RTGS
        SettlementMode mode2 = calculationService.determineSettlementMode(BigDecimal.valueOf(250000));
        assertEquals(SettlementMode.RTGS, mode2);
    }

    @Test
    public void groupTransactionsByMerchant() {
        UnsettledTransactionDto t1 = new UnsettledTransactionDto(
                "T1", "m1@upi", BigDecimal.valueOf(100), "s1@upi", LocalDateTime.now(), "PAYMENT");
        UnsettledTransactionDto t2 = new UnsettledTransactionDto(
                "T2", "m2@upi", BigDecimal.valueOf(200), "s2@upi", LocalDateTime.now(), "PAYMENT");
        UnsettledTransactionDto t3 = new UnsettledTransactionDto(
                "T3", "m1@upi", BigDecimal.valueOf(150), "s3@upi", LocalDateTime.now(), "PAYMENT");

        Map<String, List<UnsettledTransactionDto>> groups = 
                calculationService.groupTransactionsByMerchant(Arrays.asList(t1, t2, t3));

        assertNotNull(groups);
        assertEquals(2, groups.size());
        assertEquals(2, groups.get("m1@upi").size());
        assertEquals(1, groups.get("m2@upi").size());
    }
}
