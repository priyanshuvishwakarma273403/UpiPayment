package com.upimesh.settlement.service;

import com.upimesh.settlement.feign.TransactionServiceClient.UnsettledTransactionDto;
import com.upimesh.settlement.model.enums.SettlementMode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SettlementCalculationService {

    public static final BigDecimal PLATFORM_FEE_PERCENT = BigDecimal.valueOf(0.002);
    public static final BigDecimal GST_ON_FEE_PERCENT = BigDecimal.valueOf(0.18);
    public static final BigDecimal MIN_FEE = new BigDecimal("1.00");
    public static final BigDecimal MAX_FEE = new BigDecimal("1000.00");

    public BigDecimal calculatePlatformFee(BigDecimal grossAmount) {
        if (grossAmount == null || grossAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal rawFee = grossAmount.multiply(PLATFORM_FEE_PERCENT).setScale(2, RoundingMode.HALF_UP);
        if (rawFee.compareTo(MIN_FEE) < 0) {
            return MIN_FEE.setScale(2, RoundingMode.HALF_UP);
        }
        if (rawFee.compareTo(MAX_FEE) > 0) {
            return MAX_FEE.setScale(2, RoundingMode.HALF_UP);
        }
        return rawFee;
    }

    public BigDecimal calculateGst(BigDecimal fee) {
        if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return fee.multiply(GST_ON_FEE_PERCENT).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateNetAmount(BigDecimal gross, BigDecimal fee, BigDecimal gst) {
        BigDecimal net = gross.subtract(fee).subtract(gst);
        return net.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : net.setScale(2, RoundingMode.HALF_UP);
    }

    public SettlementMode determineSettlementMode(BigDecimal amount) {
        if (amount == null) {
            return SettlementMode.IMPS;
        }
        if (amount.compareTo(BigDecimal.valueOf(200000)) >= 0) {
            return SettlementMode.RTGS;
        } else if (amount.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            return SettlementMode.NEFT;
        } else {
            return SettlementMode.IMPS;
        }
    }

    public Map<String, List<UnsettledTransactionDto>> groupTransactionsByMerchant(
            List<UnsettledTransactionDto> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(UnsettledTransactionDto::merchantUpiId));
    }
}
