package com.upimesh.invoice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class QrCodeService {

    /**
     * Generates a PNG QR code image byte array for the given content.
     */
    public byte[] generateQrCode(String content, int width, int height) {
        log.info("Generating QR code image for content: {} ({}x{})", content, width, height);
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate QR Code: {}", e.getMessage(), e);
            throw new RuntimeException("QR Code generation failed: " + e.getMessage());
        }
    }
}
