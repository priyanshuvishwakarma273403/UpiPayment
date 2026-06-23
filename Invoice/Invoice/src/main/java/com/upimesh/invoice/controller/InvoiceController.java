package com.upimesh.invoice.controller;

import com.upimesh.invoice.model.request.CreateInvoiceRequest;
import com.upimesh.invoice.model.response.ApiResponse;
import com.upimesh.invoice.model.response.InvoiceResponse;
import com.upimesh.invoice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invoice")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request) {
        log.info("REST request to create invoice for customer: {}", request.getCustomerUpiId());
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Invoice draft created successfully"));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(
            @PathVariable String invoiceId) {
        log.info("REST request to get invoice details: {}", invoiceId);
        InvoiceResponse response = invoiceService.getInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(response, "Invoice details fetched successfully"));
    }

    @GetMapping(value = "/{invoiceId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable String invoiceId) {
        log.info("REST request to download invoice PDF: {}", invoiceId);
        
        // Fetch raw PDF byte array
        byte[] pdfBytes = invoiceService.getInvoicePdf(invoiceId);
        InvoiceResponse response = invoiceService.getInvoice(invoiceId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice-" + response.getInvoiceNumber().replace("/", "_") + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping("/{invoiceId}/send")
    public ResponseEntity<ApiResponse<InvoiceResponse>> sendInvoice(
            @PathVariable String invoiceId) {
        log.info("REST request to send invoice (generate payment links): {}", invoiceId);
        InvoiceResponse response = invoiceService.sendInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(response, "Invoice sent successfully, payment link generated"));
    }

    @PostMapping("/{invoiceId}/mark-paid")
    public ResponseEntity<ApiResponse<InvoiceResponse>> markAsPaid(
            @PathVariable String invoiceId,
            @RequestParam("transactionId") String transactionId) {
        log.info("REST request to mark invoice {} as paid | transactionId: {}", invoiceId, transactionId);
        InvoiceResponse response = invoiceService.markAsPaid(invoiceId, transactionId);
        return ResponseEntity.ok(ApiResponse.success(response, "Invoice paid successfully"));
    }

    @GetMapping("/merchant/{merchantUpiId}")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByMerchant(
            @PathVariable String merchantUpiId) {
        log.info("REST request to list invoices for merchant: {}", merchantUpiId);
        List<InvoiceResponse> responses = invoiceService.getInvoicesByMerchant(merchantUpiId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Merchant invoices fetched successfully"));
    }

    @PostMapping("/check-overdue")
    public ResponseEntity<ApiResponse<Void>> checkOverdueInvoices() {
        log.info("REST request to manually check overdue invoices");
        invoiceService.checkOverdueInvoices();
        return ResponseEntity.ok(ApiResponse.success(null, "Overdue invoices processed successfully"));
    }
}
