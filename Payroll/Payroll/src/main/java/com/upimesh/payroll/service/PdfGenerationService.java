package com.upimesh.payroll.service;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.upimesh.payroll.model.entity.EmployeePayment;
import com.upimesh.payroll.model.entity.PayrollBatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PdfGenerationService {

    /**
     * Generates a salary slip PDF using iText 7.
     */
    public byte[] generateSalarySlipPdf(EmployeePayment payment, PayrollBatch batch) {
        log.info("Generating PDF salary slip for payment ID: {}", payment.getPaymentId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(36f, 36f, 36f, 36f);

            // 1. Header Title Block
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{60f, 40f}));
            headerTable.setWidth(UnitValue.createPercentValue(100f));
            
            headerTable.addCell(new Cell()
                    .add(new Paragraph("SALARY SLIP")
                            .setFontSize(20f)
                            .setBold())
                    .setBorder(null));

            headerTable.addCell(new Cell()
                    .add(new Paragraph("UPI Mesh Payroll Portal")
                            .setFontSize(11f)
                            .setItalic()
                            .setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(null));
            
            doc.add(headerTable);
            doc.add(new Paragraph("\n"));

            // 2. Metadata details (Company / Employee Info)
            Table metaTable = new Table(UnitValue.createPercentArray(new float[]{50f, 50f}));
            metaTable.setWidth(UnitValue.createPercentValue(100f));

            Cell leftCell = new Cell().setBorder(null);
            leftCell.add(new Paragraph("Employee Information:").setBold().setFontSize(11f));
            leftCell.add(new Paragraph("Name: " + payment.getEmployeeName()));
            leftCell.add(new Paragraph("Employee ID: " + payment.getEmployeeId()));
            leftCell.add(new Paragraph("UPI ID: " + payment.getEmployeeUpiId()));
            leftCell.add(new Paragraph("Bank Account: " + payment.getBankAccount()));
            metaTable.addCell(leftCell);

            Cell rightCell = new Cell().setBorder(null).setTextAlignment(TextAlignment.RIGHT);
            rightCell.add(new Paragraph("Payroll Details:").setBold().setFontSize(11f));
            rightCell.add(new Paragraph("Company ID: " + batch.getCompanyId()));
            rightCell.add(new Paragraph("Month: " + batch.getMonth()));
            rightCell.add(new Paragraph("Payment ID: " + payment.getPaymentId()));
            if (payment.getProcessedAt() != null) {
                rightCell.add(new Paragraph("Paid On: " + payment.getProcessedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))));
            }
            metaTable.addCell(rightCell);

            doc.add(metaTable);
            doc.add(new Paragraph("\n"));

            // 3. Salary Details Breakdown Table
            Table breakdownTable = new Table(UnitValue.createPercentArray(new float[]{50f, 50f}));
            breakdownTable.setWidth(UnitValue.createPercentValue(100f));

            // Headers
            breakdownTable.addCell(new Cell().add(new Paragraph("Earnings").setBold().setFontSize(11f)));
            breakdownTable.addCell(new Cell().add(new Paragraph("Deductions").setBold().setFontSize(11f)));

            // Basic / PF
            breakdownTable.addCell(new Cell().add(new Paragraph("Basic Salary: ₹" + payment.getGrossSalary().multiply(java.math.BigDecimal.valueOf(0.50)))));
            breakdownTable.addCell(new Cell().add(new Paragraph("Provident Fund (PF): ₹" + payment.getPfDeduction())));

            // Gross / ESI
            breakdownTable.addCell(new Cell().add(new Paragraph("Gross Salary: ₹" + payment.getGrossSalary()).setBold()));
            breakdownTable.addCell(new Cell().add(new Paragraph("Employee State Insurance (ESI): ₹" + payment.getEsiDeduction())));

            // Empty / TDS
            breakdownTable.addCell(new Cell().add(new Paragraph("")));
            breakdownTable.addCell(new Cell().add(new Paragraph("Tax Deducted at Source (TDS): ₹" + payment.getTdsDeduction())));

            // Empty / Other deductions
            breakdownTable.addCell(new Cell().add(new Paragraph("")));
            breakdownTable.addCell(new Cell().add(new Paragraph("Other Deductions: ₹" + payment.getOtherDeductions())));

            // Totals row
            breakdownTable.addCell(new Cell().add(new Paragraph("Total Earnings: ₹" + payment.getGrossSalary()).setBold()));
            breakdownTable.addCell(new Cell().add(new Paragraph("Total Deductions: ₹" + payment.getPfDeduction().add(payment.getEsiDeduction()).add(payment.getTdsDeduction()).add(payment.getOtherDeductions())).setBold()));

            doc.add(breakdownTable);
            doc.add(new Paragraph("\n"));

            // 4. Net Salary Block
            Table netTable = new Table(UnitValue.createPercentArray(new float[]{100f}));
            netTable.setWidth(UnitValue.createPercentValue(100f));
            Cell netCell = new Cell()
                    .add(new Paragraph("NET DISBURSED SALARY: ₹" + payment.getNetSalary())
                            .setFontSize(14f)
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER));
            netTable.addCell(netCell);
            doc.add(netTable);
            doc.add(new Paragraph("\n"));

            // 5. Footer Notes
            Paragraph footer = new Paragraph("This is a computer-generated salary slip and does not require a physical signature.")
                    .setFontSize(9f)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to compile pay slip PDF for payment ID: {}", payment.getPaymentId(), e);
            throw new RuntimeException("PDF compilation failed: " + e.getMessage(), e);
        }
    }
}
