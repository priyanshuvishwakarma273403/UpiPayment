package com.merchantService.service;

import com.merchantService.dto.request.MerchantRegisterRequest;
import com.merchantService.dto.response.MerchantResponse;
import com.merchantService.dto.response.QrResponse;
import com.merchantService.entity.Merchant;
import com.merchantService.entity.MerchantQR;
import com.merchantService.exception.MerchantException;
import com.merchantService.repository.MerchantQRRepository;
import com.merchantService.repository.MerchantRepository;
import com.merchantService.util.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantQRRepository merchantQRRepository;
    private final QrCodeGenerator qrCodeGenerator;

    // ================================================================
    // 1. MERCHANT REGISTRATION
    // ================================================================

    @Transactional
    public MerchantResponse registerMerchant(MerchantRegisterRequest request) {
        log.info("Registering merchant: {}", request.getBusinessName());

        if (merchantRepository.existsByEmail(request.getEmail())) {
            throw new MerchantException("Email already registered: " + request.getEmail());
        }
        if (merchantRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new MerchantException("Phone number already registered");
        }

        // Unique merchant code generate karo
        String merchantCode = generateMerchantCode();
        String merchantUpiId = merchantCode.toLowerCase() + "@upimesh";

        Merchant merchant = Merchant.builder()
                .merchantCode(merchantCode)
                .businessName(request.getBusinessName())
                .ownerName(request.getOwnerName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .merchantUpiId(merchantUpiId)
                .businessType(request.getBusinessType())
                .gstin(request.getGstin())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankIfsc(request.getBankIfsc())
                .status(Merchant.MerchantStatus.PENDING_VERIFICATION)
                .isActive(false)
                .build();

        merchant = merchantRepository.save(merchant);
        log.info("Merchant registered: id={}, code={}, upiId={}",
                merchant.getId(), merchantCode, merchantUpiId);

        // Auto-generate static QR after registration
        generateStaticQrInternal(merchant, null, "Pay to " + request.getBusinessName());

        return MerchantResponse.fromEntity(merchant);
    }

    // ================================================================
    // 2. GET MERCHANT QR
    // ================================================================

    @Transactional(readOnly = true)
    public QrResponse getMerchantQr(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantException("Merchant not found: " + merchantId,
                        HttpStatus.NOT_FOUND));

        // Active static QR dhundho
        List<MerchantQR> qrCodes = merchantQRRepository
                .findByMerchantIdAndQrType(merchantId, MerchantQR.QrType.STATIC);

        if (qrCodes.isEmpty()) {
            // Nahi mila to generate karo
            return generateStaticQrInternal(merchant, null, "Pay to " + merchant.getBusinessName());
        }

        MerchantQR qr = qrCodes.get(0);
        return buildQrResponse(qr, merchant);
    }

    // ================================================================
    // 3. GENERATE DYNAMIC QR (Per-transaction)
    // ================================================================

    @Transactional
    public QrResponse generateDynamicQr(Long merchantId, BigDecimal amount, String description) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantException("Merchant not found: " + merchantId,
                        HttpStatus.NOT_FOUND));

        if (!merchant.getIsActive()) {
            throw new MerchantException("Merchant is not active", HttpStatus.FORBIDDEN);
        }

        String qrRefId = "QR-DYN-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase();

        String upiDeepLink = qrCodeGenerator.buildUpiDeepLink(
                merchant.getMerchantUpiId(), merchant.getBusinessName(),
                amount, description, qrRefId);

        String qrImageBase64 = qrCodeGenerator.generateQrBase64(upiDeepLink);

        MerchantQR dynamicQr = MerchantQR.builder()
                .qrReferenceId(qrRefId)
                .merchant(merchant)
                .qrType(MerchantQR.QrType.DYNAMIC)
                .amount(amount)
                .description(description)
                .upiDeepLink(upiDeepLink)
                .qrImageBase64(qrImageBase64)
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusMinutes(15)) // 15 min validity
                .build();

        dynamicQr = merchantQRRepository.save(dynamicQr);
        log.info("Dynamic QR generated: merchantId={}, amount={}, ref={}",
                merchantId, amount, qrRefId);

        return buildQrResponse(dynamicQr, merchant);
    }

    // ================================================================
    // 4. MERCHANT SETTLEMENT REPORT
    // ================================================================

    @Transactional(readOnly = true)
    public MerchantResponse getMerchantById(Long merchantId) {
        return merchantRepository.findById(merchantId)
                .map(MerchantResponse::fromEntity)
                .orElseThrow(() -> new MerchantException("Merchant not found: " + merchantId,
                        HttpStatus.NOT_FOUND));
    }

    // ================================================================
    // PRIVATE HELPERS
    // ================================================================

    private QrResponse generateStaticQrInternal(Merchant merchant,
                                                BigDecimal amount, String description) {
        String qrRefId = "QR-STA-" + merchant.getMerchantCode();

        String upiDeepLink = qrCodeGenerator.buildUpiDeepLink(
                merchant.getMerchantUpiId(), merchant.getBusinessName(),
                amount, description, null);

        String qrImageBase64 = qrCodeGenerator.generateQrBase64(upiDeepLink);

        MerchantQR staticQr = MerchantQR.builder()
                .qrReferenceId(qrRefId)
                .merchant(merchant)
                .qrType(MerchantQR.QrType.STATIC)
                .amount(amount)
                .description(description)
                .upiDeepLink(upiDeepLink)
                .qrImageBase64(qrImageBase64)
                .isActive(true)
                .build();

        staticQr = merchantQRRepository.save(staticQr);
        log.info("Static QR generated for merchantId={}", merchant.getId());
        return buildQrResponse(staticQr, merchant);
    }

    private QrResponse buildQrResponse(MerchantQR qr, Merchant merchant) {
        return QrResponse.builder()
                .id(qr.getId())
                .qrReferenceId(qr.getQrReferenceId())
                .merchantId(merchant.getId())
                .merchantUpiId(merchant.getMerchantUpiId())
                .businessName(merchant.getBusinessName())
                .qrType(qr.getQrType().name())
                .amount(qr.getAmount())
                .description(qr.getDescription())
                .upiDeepLink(qr.getUpiDeepLink())
                .qrImageBase64("data:image/png;base64," + qr.getQrImageBase64())
                .isActive(qr.getIsActive())
                .expiresAt(qr.getExpiresAt())
                .createdAt(qr.getCreatedAt())
                .build();
    }

    private String generateMerchantCode() {
        String code = "MERCH" + String.format("%05d",
                (int)(Math.random() * 99999) + 1);
        // Collision check
        while (merchantRepository.findByMerchantCode(code).isPresent()) {
            code = "MERCH" + String.format("%05d", (int)(Math.random() * 99999) + 1);
        }
        return code;
    }
}
