package com.upimesh.rewards.service;

import com.upimesh.rewards.model.entity.Offer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class PointsCalculationService {

    /**
     * Calculates points earned on a transaction: 1 point per ₹10 spent.
     */
    public int calculatePoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        // (int)(amount.doubleValue() / 10)
        return amount.divide(BigDecimal.valueOf(10), 0, RoundingMode.DOWN).intValue();
    }

    /**
     * Calculates cashback based on an active offer details, enforcing min amount thresholds and maximum cashback capping.
     */
    public BigDecimal calculateCashback(BigDecimal amount, Offer offer) {
        if (amount == null || offer == null || !offer.isActive()) {
            return BigDecimal.ZERO;
        }

        // Validate min transaction amount threshold
        if (amount.compareTo(offer.getMinTransactionAmount()) < 0) {
            log.info("Transaction amount ₹{} is below min threshold ₹{} for offer: {}", 
                    amount, offer.getMinTransactionAmount(), offer.getOfferId());
            return BigDecimal.ZERO;
        }

        // Calculate discount percent
        BigDecimal discount = amount.multiply(offer.getDiscountPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Enforce maximum capping limit
        if (discount.compareTo(offer.getMaxDiscountAmount()) > 0) {
            log.info("Discount ₹{} exceeds max discount cap ₹{} for offer: {}. Capping to max.", 
                    discount, offer.getMaxDiscountAmount(), offer.getOfferId());
            return offer.getMaxDiscountAmount();
        }

        return discount;
    }
}
