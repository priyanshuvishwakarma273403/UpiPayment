package com.upimesh.invoice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_line_items", indexes = {
        @Index(name = "idx_item_invoice_id", columnList = "invoiceId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String invoiceId;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal gstRate; // rate percentage, e.g. 18.00 for 18%

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount; // final amount before GST: quantity * unitPrice

    @Column(length = 20)
    private String hsnCode;
}
