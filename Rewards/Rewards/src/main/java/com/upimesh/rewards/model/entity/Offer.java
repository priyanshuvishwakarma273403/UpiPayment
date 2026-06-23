package com.upimesh.rewards.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "offers", indexes = {
        @Index(name = "idx_offer_id", columnList = "offerId", unique = true),
        @Index(name = "idx_offer_merchant", columnList = "merchantUpiId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String offerId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 200)
    private String description;

    @Column(length = 100)
    private String merchantUpiId; // Nullable if offer is platform-wide (applicable to all merchants)

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal minTransactionAmount;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validTo;

    @Column(nullable = false)
    private int usageLimit;

    @Column(nullable = false)
    private int usedCount;

    @Column(nullable = false)
    private boolean isActive;
}
