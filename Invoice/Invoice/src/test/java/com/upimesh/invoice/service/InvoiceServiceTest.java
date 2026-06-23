package com.upimesh.invoice.service;

import com.upimesh.invoice.model.entity.Invoice;
import com.upimesh.invoice.model.entity.InvoiceLineItem;
import com.upimesh.invoice.model.enums.GstType;
import com.upimesh.invoice.model.enums.InvoiceStatus;
import com.upimesh.invoice.model.request.CreateInvoiceRequest;
import com.upimesh.invoice.model.request.LineItemRequest;
import com.upimesh.invoice.model.response.InvoiceResponse;
import com.upimesh.invoice.repository.InvoiceLineItemRepository;
import com.upimesh.invoice.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private InvoiceLineItemRepository lineItemRepository;
    @Mock private PdfGenerationService pdfGenerationService;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void testCreateInvoice_Success() {
        LineItemRequest itemRequest = LineItemRequest.builder()
                .description("Off-grid node router")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("1000.00"))
                .gstRate(new BigDecimal("18.00"))
                .hsnCode("8517")
                .build();

        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .merchantUpiId("merchant@upimesh")
                .merchantName("Mesh Tech")
                .customerUpiId("customer@upimesh")
                .customerName("Rahul Sharma")
                .dueDate(LocalDate.now().plusDays(10))
                .gstType(GstType.CGST_SGST)
                .lineItems(Collections.singletonList(itemRequest))
                .build();

        when(invoiceRepository.findFirstByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(anyString()))
                .thenReturn(Optional.empty());

        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice saved = inv.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        InvoiceResponse response = invoiceService.createInvoice(request);

        assertNotNull(response);
        assertEquals(InvoiceStatus.DRAFT, response.getStatus());
        assertEquals(new BigDecimal("1000.00"), response.getSubtotal());
        assertEquals(new BigDecimal("90.00"), response.getCgst());
        assertEquals(new BigDecimal("90.00"), response.getSgst());
        assertEquals(BigDecimal.ZERO, response.getIgst());
        assertEquals(new BigDecimal("1180.00"), response.getTotalAmount());
        assertTrue(response.getInvoiceNumber().contains("/000001"));

        verify(invoiceRepository, times(1)).save(any(Invoice.class));
        verify(lineItemRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testGenerateInvoicePdf() {
        Invoice invoice = Invoice.builder()
                .invoiceId("INV123")
                .invoiceNumber("INV/2026/000001")
                .status(InvoiceStatus.DRAFT)
                .build();

        InvoiceLineItem item = InvoiceLineItem.builder()
                .invoiceId("INV123")
                .description("Off-grid router")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("1000.00"))
                .gstRate(new BigDecimal("18.00"))
                .amount(new BigDecimal("1000.00"))
                .build();

        byte[] mockPdfBytes = "PDF-CONTENT".getBytes();

        when(invoiceRepository.findByInvoiceId("INV123")).thenReturn(Optional.of(invoice));
        when(lineItemRepository.findByInvoiceId("INV123")).thenReturn(Collections.singletonList(item));
        when(pdfGenerationService.generateInvoicePdf(eq(invoice), anyList())).thenReturn(mockPdfBytes);

        byte[] result = invoiceService.getInvoicePdf("INV123");

        assertArrayEquals(mockPdfBytes, result);
        verify(pdfGenerationService, times(1)).generateInvoicePdf(eq(invoice), anyList());
    }

    @Test
    void testMarkAsPaid() {
        Invoice invoice = Invoice.builder()
                .invoiceId("INV123")
                .status(InvoiceStatus.SENT)
                .build();

        InvoiceLineItem item = InvoiceLineItem.builder()
                .invoiceId("INV123")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("100.00"))
                .gstRate(BigDecimal.ZERO)
                .amount(new BigDecimal("100.00"))
                .build();

        when(invoiceRepository.findByInvoiceId("INV123")).thenReturn(Optional.of(invoice));
        when(lineItemRepository.findByInvoiceId("INV123")).thenReturn(Collections.singletonList(item));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceResponse response = invoiceService.markAsPaid("INV123", "TXN999");

        assertEquals(InvoiceStatus.PAID, response.getStatus());
        assertEquals("TXN999", response.getTransactionId());
        assertNotNull(response.getPaidAt());
        verify(invoiceRepository, times(1)).save(invoice);
    }

    @Test
    void testSendInvoice() {
        Invoice invoice = Invoice.builder()
                .invoiceId("INV123")
                .invoiceNumber("INV/2026/000001")
                .merchantUpiId("merchant@upimesh")
                .merchantName("Mesh Tech")
                .totalAmount(new BigDecimal("1180.00"))
                .status(InvoiceStatus.DRAFT)
                .build();

        InvoiceLineItem item = InvoiceLineItem.builder()
                .invoiceId("INV123")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("1000.00"))
                .gstRate(new BigDecimal("18.00"))
                .amount(new BigDecimal("1000.00"))
                .build();

        when(invoiceRepository.findByInvoiceId("INV123")).thenReturn(Optional.of(invoice));
        when(lineItemRepository.findByInvoiceId("INV123")).thenReturn(Collections.singletonList(item));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceResponse response = invoiceService.sendInvoice("INV123");

        assertEquals(InvoiceStatus.SENT, response.getStatus());
        assertNotNull(response.getPaymentLink());
        assertTrue(response.getPaymentLink().contains("upi://pay?pa=merchant@upimesh"));
        assertTrue(response.getPaymentLink().contains("am=1180.00"));
        verify(invoiceRepository, times(1)).save(invoice);
    }

    @Test
    void testCheckOverdueInvoices() {
        Invoice overdueInvoice = Invoice.builder()
                .invoiceId("INV123")
                .invoiceNumber("INV/2026/000001")
                .status(InvoiceStatus.SENT)
                .dueDate(LocalDate.now().minusDays(1))
                .build();

        when(invoiceRepository.findByStatusAndDueDateBefore(eq(InvoiceStatus.SENT), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(overdueInvoice));

        invoiceService.checkOverdueInvoices();

        assertEquals(InvoiceStatus.OVERDUE, overdueInvoice.getStatus());
        verify(invoiceRepository, times(1)).save(overdueInvoice);
    }
}
