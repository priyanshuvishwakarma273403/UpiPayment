package com.merchantService.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

/**
 * ================================================================
 * QR Code Generator - ZXing Library
 * ================================================================
 * UPI QR Code format:
 * upi://pay?pa=<UPI_ID>&pn=<NAME>&am=<AMOUNT>&cu=INR&tn=<NOTE>
 *
 * pa = payee address (UPI ID)
 * pn = payee name
 * am = amount (optional for static QR)
 * cu = currency (always INR)
 * tn = transaction note
 * tr = transaction reference ID
 *
 * ZXing: Google's barcode library
 * - QRCodeWriter: QR code generate karta hai
 * - MatrixToImageWriter: BitMatrix ko PNG image banata hai
 * ================================================================
 */

@Component
@Slf4j
public class QrCodeGenerator {
    private static final int QR_WIDTH  = 300; // pixels
    private static final int QR_HEIGHT = 300;
    private static final String IMAGE_FORMAT = "PNG";

    /**
     * Static QR generate karo (any amount ya fixed amount)
     * Returns: Base64 encoded PNG image
     */
    public String generateStaticQr(String merchantUpiId, String merchantName,
                                   BigDecimal fixedAmount, String description) {
        String upiDeepLink = buildUpiDeepLink(merchantUpiId, merchantName, fixedAmount, description, null);
        return generateQrBase64(upiDeepLink);
    }

    /**
     * Dynamic QR generate karo (specific transaction ke liye)
     */
    public String generateDynamicQr(String merchantUpiId, String merchantName,
                                    BigDecimal amount, String description,
                                    String transactionRef) {
        String upiDeepLink = buildUpiDeepLink(merchantUpiId, merchantName, amount, description, transactionRef);
        return generateQrBase64(upiDeepLink);
    }

    /**
     * UPI Deep Link string build karo
     * Standard format: upi://pay?pa=...&pn=...&am=...&cu=INR&tn=...
     */
    public String buildUpiDeepLink(String upiId, String payeeName,
                                   BigDecimal amount, String note,
                                   String transactionRef) {
        StringBuilder sb = new StringBuilder("upi://pay?");
        sb.append("pa=").append(encodeParam(upiId));
        sb.append("&pn=").append(encodeParam(payeeName));
        if (amount != null) {
            sb.append("&am=").append(amount.toPlainString());
        }
        sb.append("&cu=INR");
        if (note != null && !note.isBlank()) {
            sb.append("&tn=").append(encodeParam(note));
        }
        if (transactionRef != null && !transactionRef.isBlank()) {
            sb.append("&tr=").append(encodeParam(transactionRef));
        }
        return sb.toString();
    }

    /**
     * QR string se Base64 PNG image generate karo
     */
    public String generateQrBase64(String content) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE,
                    QR_WIDTH, QR_HEIGHT, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);

            String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            log.debug("QR code generated successfully, size: {} bytes", outputStream.size());
            return base64;

        } catch (WriterException | IOException e) {
            log.error("QR code generation failed: {}", e.getMessage());
            throw new RuntimeException("QR code generation failed", e);
        }
    }

    private String encodeParam(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}