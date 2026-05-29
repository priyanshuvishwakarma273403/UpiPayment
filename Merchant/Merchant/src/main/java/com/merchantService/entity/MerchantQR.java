package com.merchantService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ================================================================
 * MerchantQR Entity - MySQL Table: merchant_qr_codes
 * ================================================================
 * Merchant ke QR codes yahan store hote hain.
 * Types:
 * - STATIC:  Fixed amount (e.g. ₹50 tea), ya any amount (merchant set karta hai)
 * - DYNAMIC: Per-transaction generate hota hai (amount specific)
 *
 * QR Data Format (UPI Deep Link):
 * upi://pay?pa=merchantCode@upimesh&pn=BusinessName&am=500&cu=INR&tn=Description
 * ================================================================
 */
@Entity
@Table(name = "merchant_qr_codes",
        indexes = {
                @Index(name = "idx_qr_merchant_id",  columnList = "merchant_id"),
                @Index(name = "idx_qr_reference_id", columnList = "qr_reference_id", unique = true)
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantQR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "qr_reference_id", unique = true, nullable = false, length = 60)
    private String qrReferenceId;       // Unique ID for this QR

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Enumerated(EnumType.STRING)
    @Column(name = "qr_type", nullable = false, length = 10)
    private QrType qrType;

    // STATIC QR ke liye fixed amount (null = any amount)
    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    // QR ki description (e.g. "Order #123")
    @Column(length = 200)
    private String description;

    // Base64 encoded PNG image of QR code
    @Column(name = "qr_image_base64", columnDefinition = "LONGTEXT")
    private String qrImageBase64;

    // UPI deep link string
    @Column(name = "upi_deep_link", length = 500)
    private String upiDeepLink;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // DYNAMIC QR - expire time
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Kitni baar scan hua
    @Column(name = "scan_count")
    @Builder.Default
    private Integer scanCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum QrType {
        STATIC,   // Merchant ki dukaan par permanently laga rahega
        DYNAMIC   // Per-transaction generate hota hai
    }

}
