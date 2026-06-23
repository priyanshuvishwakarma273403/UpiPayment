package com.upimesh.invoice.service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.upimesh.invoice.model.entity.Invoice;
import com.upimesh.invoice.model.entity.InvoiceLineItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationService {

    private final QrCodeService qrCodeService;

    /**
     * Generates a tax invoice PDF using iText 7.
     */
    public byte[] generateInvoicePdf(Invoice invoice, List<InvoiceLineItem> items) {
        log.info("Generating PDF invoice for invoiceNumber: {}", invoice.getInvoiceNumber());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(30f, 30f, 30f, 30f);

            // 1. Header Title
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{50f, 50f}));
            headerTable.setWidth(UnitValue.createPercentValue(100f));
            
            headerTable.addCell(new Cell()
                    .add(new Paragraph("TAX INVOICE")
                            .setFontSize(24f)
                            .setBold())
                    .setBorder(null));

            headerTable.addCell(new Cell()
                    .add(new Paragraph("UPI Mesh Payments")
                            .setFontSize(14f)
                            .setItalic()
                            .setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(null));
            
            doc.add(headerTable);
            doc.add(new Paragraph("\n"));

            // 2. Invoice Details (Metadata Table)
            Table metaTable = new Table(UnitValue.createPercentArray(new float[]{50f, 50f}));
            metaTable.setWidth(UnitValue.createPercentValue(100f));

            Cell metaLeft = new Cell().setBorder(null);
            metaLeft.add(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber()).setBold());
            metaLeft.add(new Paragraph("Date: " + invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))));
            metaLeft.add(new Paragraph("Due Date: " + invoice.getDueDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
            if (invoice.getIrpAckNumber() != null) {
                metaLeft.add(new Paragraph("IRP Ack No: " + invoice.getIrpAckNumber()).setFontSize(9f));
            }
            metaTable.addCell(metaLeft);

            Cell metaRight = new Cell().setBorder(null).setTextAlignment(TextAlignment.RIGHT);
            metaRight.add(new Paragraph("Status: " + invoice.getStatus().name()).setBold().setFontSize(12f));
            if (invoice.getTransactionId() != null) {
                metaRight.add(new Paragraph("Payment Txn ID: " + invoice.getTransactionId()));
                metaRight.add(new Paragraph("Paid At: " + invoice.getPaidAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))));
            }
            metaTable.addCell(metaRight);

            doc.add(metaTable);
            doc.add(new Paragraph("\n"));

            // 3. Merchant & Customer Details Table
            Table partiesTable = new Table(UnitValue.createPercentArray(new float[]{50f, 50f}));
            partiesTable.setWidth(UnitValue.createPercentValue(100f));

            Cell merchantCell = new Cell().setBorder(null);
            merchantCell.add(new Paragraph("Sender (Merchant):").setBold().setFontSize(11f));
            merchantCell.add(new Paragraph(invoice.getMerchantName()));
            merchantCell.add(new Paragraph("UPI ID: " + invoice.getMerchantUpiId()));
            if (invoice.getMerchantGstin() != null && !invoice.getMerchantGstin().isEmpty()) {
                merchantCell.add(new Paragraph("GSTIN: " + invoice.getMerchantGstin()));
            }
            partiesTable.addCell(merchantCell);

            Cell customerCell = new Cell().setBorder(null);
            customerCell.add(new Paragraph("Receiver (Customer):").setBold().setFontSize(11f));
            customerCell.add(new Paragraph(invoice.getCustomerName()));
            customerCell.add(new Paragraph("UPI ID: " + invoice.getCustomerUpiId()));
            if (invoice.getCustomerGstin() != null && !invoice.getCustomerGstin().isEmpty()) {
                customerCell.add(new Paragraph("GSTIN: " + invoice.getCustomerGstin()));
            }
            partiesTable.addCell(customerCell);

            doc.add(partiesTable);
            doc.add(new Paragraph("\n"));

            // 4. Line Items Table (GST details split)
            Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{8f, 27f, 15f, 10f, 12f, 8f, 10f, 10f}));
            itemsTable.setWidth(UnitValue.createPercentValue(100f));

            // Header row
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("#").setBold()).setTextAlignment(TextAlignment.CENTER));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("HSN Code").setBold()).setTextAlignment(TextAlignment.CENTER));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Qty").setBold()).setTextAlignment(TextAlignment.RIGHT));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Rate (₹)").setBold()).setTextAlignment(TextAlignment.RIGHT));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("GST %").setBold()).setTextAlignment(TextAlignment.RIGHT));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("GST (₹)").setBold()).setTextAlignment(TextAlignment.RIGHT));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Total (₹)").setBold()).setTextAlignment(TextAlignment.RIGHT));

            int count = 1;
            for (InvoiceLineItem item : items) {
                // GST calculations
                BigDecimal rateMultiplier = item.getGstRate().divide(BigDecimal.valueOf(100));
                BigDecimal itemGst = item.getAmount().multiply(rateMultiplier);
                BigDecimal itemTotal = item.getAmount().add(itemGst);

                itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(count++))).setTextAlignment(TextAlignment.CENTER));
                itemsTable.addCell(new Cell().add(new Paragraph(item.getDescription())));
                itemsTable.addCell(new Cell().add(new Paragraph(item.getHsnCode() != null ? item.getHsnCode() : "-")).setTextAlignment(TextAlignment.CENTER));
                itemsTable.addCell(new Cell().add(new Paragraph(item.getQuantity().stripTrailingZeros().toPlainString())).setTextAlignment(TextAlignment.RIGHT));
                itemsTable.addCell(new Cell().add(new Paragraph(item.getUnitPrice().setScale(2).toPlainString())).setTextAlignment(TextAlignment.RIGHT));
                itemsTable.addCell(new Cell().add(new Paragraph(item.getGstRate().stripTrailingZeros().toPlainString() + "%")).setTextAlignment(TextAlignment.RIGHT));
                itemsTable.addCell(new Cell().add(new Paragraph(itemGst.setScale(2).toPlainString())).setTextAlignment(TextAlignment.RIGHT));
                itemsTable.addCell(new Cell().add(new Paragraph(itemTotal.setScale(2).toPlainString())).setTextAlignment(TextAlignment.RIGHT));
            }

            doc.add(itemsTable);
            doc.add(new Paragraph("\n"));

            // 5. Summary Section & Payment QR Code (Side-by-Side Table)
            Table footerTable = new Table(UnitValue.createPercentArray(new float[]{60f, 40f}));
            footerTable.setWidth(UnitValue.createPercentValue(100f));

            Cell footerLeft = new Cell().setBorder(null);
            footerLeft.add(new Paragraph("Notes & Terms:").setBold().setFontSize(10f));
            footerLeft.add(new Paragraph(invoice.getNotes() != null ? invoice.getNotes() : "Thank you for doing business with us. Please scan the QR code to complete the payment via any UPI application.").setFontSize(9f));
            
            // Render ZXing QR code image
            try {
                String qrContent = String.format("upi://pay?pa=%s&pn=%s&am=%s&tn=INV-%s",
                        invoice.getMerchantUpiId(),
                        invoice.getMerchantName(),
                        invoice.getTotalAmount().setScale(2).toPlainString(),
                        invoice.getInvoiceNumber());
                
                byte[] qrBytes = qrCodeService.generateQrCode(qrContent, 120, 120);
                ImageData imageData = ImageDataFactory.create(qrBytes);
                Image qrImage = new Image(imageData);
                qrImage.setWidth(120f).setHeight(120f);
                
                footerLeft.add(new Paragraph("\n"));
                footerLeft.add(new Paragraph("Scan to Pay:").setBold().setFontSize(10f));
                footerLeft.add(qrImage);
            } catch (Exception e) {
                log.error("Failed to append payment QR code: {}", e.getMessage());
            }
            footerTable.addCell(footerLeft);

            Cell footerRight = new Cell().setBorder(null);
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{50f, 50f}));
            summaryTable.setWidth(UnitValue.createPercentValue(100f));

            summaryTable.addCell(new Cell().add(new Paragraph("Subtotal:")).setBorder(null));
            summaryTable.addCell(new Cell().add(new Paragraph("₹" + invoice.getSubtotal().setScale(2).toPlainString())).setBorder(null).setTextAlignment(TextAlignment.RIGHT));

            if (invoice.getCgst().compareTo(BigDecimal.ZERO) > 0) {
                summaryTable.addCell(new Cell().add(new Paragraph("CGST:")).setBorder(null));
                summaryTable.addCell(new Cell().add(new Paragraph("₹" + invoice.getCgst().setScale(2).toPlainString())).setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            }
            if (invoice.getSgst().compareTo(BigDecimal.ZERO) > 0) {
                summaryTable.addCell(new Cell().add(new Paragraph("SGST:")).setBorder(null));
                summaryTable.addCell(new Cell().add(new Paragraph("₹" + invoice.getSgst().setScale(2).toPlainString())).setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            }
            if (invoice.getIgst().compareTo(BigDecimal.ZERO) > 0) {
                summaryTable.addCell(new Cell().add(new Paragraph("IGST:")).setBorder(null));
                summaryTable.addCell(new Cell().add(new Paragraph("₹" + invoice.getIgst().setScale(2).toPlainString())).setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            }

            summaryTable.addCell(new Cell().add(new Paragraph("Grand Total:").setBold().setFontSize(12f)).setBorder(null));
            summaryTable.addCell(new Cell().add(new Paragraph("₹" + invoice.getTotalAmount().setScale(2).toPlainString()).setBold().setFontSize(12f)).setBorder(null).setTextAlignment(TextAlignment.RIGHT));

            footerRight.add(summaryTable);
            footerTable.addCell(footerRight);

            doc.add(footerTable);
            doc.close();

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF: {}", e.getMessage(), e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage());
        }
    }
}
