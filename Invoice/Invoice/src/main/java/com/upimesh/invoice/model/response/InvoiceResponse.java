package com.upimesh.invoice.model.response;

import com.upimesh.invoice.model.enums.GstType;
import com.upimesh.invoice.model.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private String invoiceId;
    private String invoiceNumber;
    private String merchantUpiId;
    private String merchantName;
    private String merchantGstin;
    private String customerUpiId;
    private String customerName;
    private String customerGstin;
    private BigDecimal subtotal;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal totalAmount;
    private GstType gstType;
    private InvoiceStatus status;
    private LocalDate dueDate;
    private LocalDateTime paidAt;
    private String transactionId;
    private String paymentLink;
    private String irpAckNumber;
    private String notes;
    private List<InvoiceLineItemResponse> lineItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
