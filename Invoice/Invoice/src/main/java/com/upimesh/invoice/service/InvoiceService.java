package com.upimesh.invoice.service;

import com.upimesh.invoice.exception.InvoiceNotFoundException;
import com.upimesh.invoice.model.entity.Invoice;
import com.upimesh.invoice.model.entity.InvoiceLineItem;
import com.upimesh.invoice.model.enums.GstType;
import com.upimesh.invoice.model.enums.InvoiceStatus;
import com.upimesh.invoice.model.request.CreateInvoiceRequest;
import com.upimesh.invoice.model.request.LineItemRequest;
import com.upimesh.invoice.model.response.InvoiceLineItemResponse;
import com.upimesh.invoice.model.response.InvoiceResponse;
import com.upimesh.invoice.repository.InvoiceLineItemRepository;
import com.upimesh.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository lineItemRepository;
    private final PdfGenerationService pdfGenerationService;

    /**
     * Creates a new GST-compliant invoice.
     */
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        log.info("Creating invoice for merchant: {} to customer: {}", 
                request.getMerchantUpiId(), request.getCustomerUpiId());

        String invoiceId = "INV" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 15).toUpperCase();

        String invoiceNumber = generateInvoiceNumber();

        // 1. Calculate GST details
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;

        List<InvoiceLineItem> lineItems = new ArrayList<>();

        for (LineItemRequest itemReq : request.getLineItems()) {
            BigDecimal baseAmount = itemReq.getUnitPrice().multiply(itemReq.getQuantity())
                    .setScale(2, RoundingMode.HALF_UP);
            
            subtotal = subtotal.add(baseAmount);

            BigDecimal gstRateMultiplier = itemReq.getGstRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal itemGst = baseAmount.multiply(gstRateMultiplier).setScale(2, RoundingMode.HALF_UP);

            if (request.getGstType() == GstType.CGST_SGST) {
                BigDecimal halfGst = itemGst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                totalCgst = totalCgst.add(halfGst);
                totalSgst = totalSgst.add(halfGst);
            } else {
                totalIgst = totalIgst.add(itemGst);
            }

            InvoiceLineItem lineItem = InvoiceLineItem.builder()
                    .invoiceId(invoiceId)
                    .description(itemReq.getDescription())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .gstRate(itemReq.getGstRate())
                    .amount(baseAmount)
                    .hsnCode(itemReq.getHsnCode())
                    .build();

            lineItems.add(lineItem);
        }

        BigDecimal totalAmount = subtotal.add(totalCgst).add(totalSgst).add(totalIgst)
                .setScale(2, RoundingMode.HALF_UP);

        // 2. Generate mock e-invoice acknowledgment
        String irpAckNumber = "ACK" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 10).toUpperCase();

        Invoice invoice = Invoice.builder()
                .invoiceId(invoiceId)
                .invoiceNumber(invoiceNumber)
                .merchantUpiId(request.getMerchantUpiId())
                .merchantName(request.getMerchantName())
                .merchantGstin(request.getMerchantGstin())
                .customerUpiId(request.getCustomerUpiId())
                .customerName(request.getCustomerName())
                .customerGstin(request.getCustomerGstin())
                .subtotal(subtotal)
                .cgst(totalCgst)
                .sgst(totalSgst)
                .igst(totalIgst)
                .totalAmount(totalAmount)
                .gstType(request.getGstType())
                .status(InvoiceStatus.DRAFT)
                .dueDate(request.getDueDate())
                .irpAckNumber(irpAckNumber)
                .notes(request.getNotes())
                .build();

        // 3. Save entities
        lineItemRepository.saveAll(lineItems);
        invoice = invoiceRepository.save(invoice);

        log.info("Invoice created successfully with ID: {} and number: {}", invoiceId, invoiceNumber);
        return mapToInvoiceResponse(invoice, lineItems);
    }

    /**
     * Marks an invoice as PAID.
     */
    @Transactional
    public InvoiceResponse markAsPaid(String invoiceId, String transactionId) {
        log.info("Marking invoice: {} as PAID | TxnId: {}", invoiceId, transactionId);
        Invoice invoice = invoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice is already marked as PAID");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setTransactionId(transactionId);
        invoice = invoiceRepository.save(invoice);

        List<InvoiceLineItem> items = lineItemRepository.findByInvoiceId(invoiceId);
        return mapToInvoiceResponse(invoice, items);
    }

    /**
     * Encodes payment links and marks status as SENT.
     */
    @Transactional
    public InvoiceResponse sendInvoice(String invoiceId) {
        log.info("Sending invoice with ID: {}", invoiceId);
        Invoice invoice = invoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + invoiceId));

        if (invoice.getStatus() != InvoiceStatus.DRAFT && invoice.getStatus() != InvoiceStatus.SENT) {
            throw new IllegalStateException("Only DRAFT or SENT invoices can be sent. Current status: " + invoice.getStatus());
        }

        // Generate payment UPI deep link
        try {
            String encodedName = URLEncoder.encode(invoice.getMerchantName(), StandardCharsets.UTF_8.name()).replace("+", "%20");
            String paymentLink = String.format("upi://pay?pa=%s&pn=%s&am=%s&tn=INV-%s",
                    invoice.getMerchantUpiId(),
                    encodedName,
                    invoice.getTotalAmount().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                    invoice.getInvoiceNumber());
            invoice.setPaymentLink(paymentLink);
        } catch (Exception e) {
            log.error("Failed to generate payment deep link: {}", e.getMessage());
            invoice.setPaymentLink("upi://pay?pa=" + invoice.getMerchantUpiId() + "&am=" + invoice.getTotalAmount().toPlainString());
        }

        invoice.setStatus(InvoiceStatus.SENT);
        invoice = invoiceRepository.save(invoice);

        List<InvoiceLineItem> items = lineItemRepository.findByInvoiceId(invoiceId);
        return mapToInvoiceResponse(invoice, items);
    }

    /**
     * Fetches invoice PDF byte array.
     */
    public byte[] getInvoicePdf(String invoiceId) {
        log.info("Fetching PDF for invoice ID: {}", invoiceId);
        Invoice invoice = invoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + invoiceId));

        List<InvoiceLineItem> items = lineItemRepository.findByInvoiceId(invoiceId);
        return pdfGenerationService.generateInvoicePdf(invoice, items);
    }

    /**
     * Scheduler checking daily for overdue invoices.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkOverdueInvoices() {
        LocalDate today = LocalDate.now();
        log.info("Checking for overdue invoices past date: {}", today);

        List<Invoice> sentInvoices = invoiceRepository
                .findByStatusAndDueDateBefore(InvoiceStatus.SENT, today);

        log.info("Found {} invoices to mark as OVERDUE", sentInvoices.size());

        for (Invoice inv : sentInvoices) {
            inv.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.save(inv);
            log.info("Invoice {} marked as OVERDUE", inv.getInvoiceNumber());
        }
    }

    public InvoiceResponse getInvoice(String invoiceId) {
        Invoice invoice = invoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + invoiceId));
        List<InvoiceLineItem> items = lineItemRepository.findByInvoiceId(invoiceId);
        return mapToInvoiceResponse(invoice, items);
    }

    public List<InvoiceResponse> getInvoicesByMerchant(String merchantUpiId) {
        return invoiceRepository.findByMerchantUpiId(merchantUpiId).stream()
                .map(inv -> {
                    List<InvoiceLineItem> items = lineItemRepository.findByInvoiceId(inv.getInvoiceId());
                    return mapToInvoiceResponse(inv, items);
                })
                .collect(Collectors.toList());
    }

    /**
     * Generates a unique, sequential invoice number.
     * Format: INV/YYYY/xxxxxx (6-digit zero padded sequence number)
     */
    private synchronized String generateInvoiceNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "INV/" + year + "/";

        Optional<Invoice> latest = invoiceRepository
                .findFirstByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(prefix);

        int nextSeq = 1;
        if (latest.isPresent()) {
            String lastNum = latest.get().getInvoiceNumber();
            try {
                String seqStr = lastNum.substring(lastNum.lastIndexOf("/") + 1);
                nextSeq = Integer.parseInt(seqStr) + 1;
            } catch (Exception e) {
                log.error("Failed to parse latest sequence from: {}", lastNum, e);
            }
        }

        return prefix + String.format("%06d", nextSeq);
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice inv, List<InvoiceLineItem> items) {
        List<InvoiceLineItemResponse> itemResponses = items.stream()
                .map(item -> InvoiceLineItemResponse.builder()
                        .description(item.getDescription())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .gstRate(item.getGstRate())
                        .amount(item.getAmount())
                        .hsnCode(item.getHsnCode())
                        .build())
                .collect(Collectors.toList());

        return InvoiceResponse.builder()
                .invoiceId(inv.getInvoiceId())
                .invoiceNumber(inv.getInvoiceNumber())
                .merchantUpiId(inv.getMerchantUpiId())
                .merchantName(inv.getMerchantName())
                .merchantGstin(inv.getMerchantGstin())
                .customerUpiId(inv.getCustomerUpiId())
                .customerName(inv.getCustomerName())
                .customerGstin(inv.getCustomerGstin())
                .subtotal(inv.getSubtotal())
                .cgst(inv.getCgst())
                .sgst(inv.getSgst())
                .igst(inv.getIgst())
                .totalAmount(inv.getTotalAmount())
                .gstType(inv.getGstType())
                .status(inv.getStatus())
                .dueDate(inv.getDueDate())
                .paidAt(inv.getPaidAt())
                .transactionId(inv.getTransactionId())
                .paymentLink(inv.getPaymentLink())
                .irpAckNumber(inv.getIrpAckNumber())
                .notes(inv.getNotes())
                .lineItems(itemResponses)
                .createdAt(inv.getCreatedAt())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }
}
