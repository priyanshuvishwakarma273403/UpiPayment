package com.upimesh.invoice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLineItemResponse {
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal gstRate;
    private BigDecimal amount;
    private String hsnCode;
}
