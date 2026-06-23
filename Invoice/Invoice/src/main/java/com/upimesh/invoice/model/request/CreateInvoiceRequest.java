package com.upimesh.invoice.model.request;

import com.upimesh.invoice.model.enums.GstType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {

    @NotBlank(message = "Merchant UPI ID is required")
    private String merchantUpiId;

    @NotBlank(message = "Merchant name is required")
    private String merchantName;

    private String merchantGstin;

    @NotBlank(message = "Customer UPI ID is required")
    private String customerUpiId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String customerGstin;

    @NotEmpty(message = "Invoice must contain at least one line item")
    @Valid
    private List<LineItemRequest> lineItems;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private String notes;

    @NotNull(message = "GST type is required")
    private GstType gstType;
}
