package com.upimesh.invoice.model.entity;

import com.upimesh.invoice.model.enums.GstType;
import com.upimesh.invoice.model.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices", indexes = {
        @Index(name = "idx_invoice_id", columnList = "invoiceId", unique = true),
        @Index(name = "idx_invoice_number", columnList = "invoiceNumber", unique = true),
        @Index(name = "idx_invoice_merchant_upi", columnList = "merchantUpiId"),
        @Index(name = "idx_invoice_customer_upi", columnList = "customerUpiId"),
        @Index(name = "idx_invoice_status", columnList = "status"),
        @Index(name = "idx_invoice_due_date", columnList = "dueDate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String invoiceId;

    @Column(nullable = false, unique = true, length = 30)
    private String invoiceNumber; // Format: INV/YYYY/xxxxxx

    @Column(nullable = false, length = 100)
    private String merchantUpiId;

    @Column(nullable = false, length = 100)
    private String merchantName;

    @Column(length = 20)
    private String merchantGstin;

    @Column(nullable = false, length = 100)
    private String customerUpiId;

    @Column(nullable = false, length = 100)
    private String customerName;

    @Column(length = 20)
    private String customerGstin;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal cgst;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal sgst;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal igst;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GstType gstType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDateTime paidAt;

    @Column(length = 50)
    private String transactionId;

    @Column(length = 200)
    private String paymentLink;

    @Column(length = 50)
    private String irpAckNumber; // GST e-invoice Ack No.

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
