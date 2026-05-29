package com.paymentService.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * ================================================================
 * Payment Signature Utility - RSA Asymmetric Encryption
 * ================================================================
 * Kyun RSA Signature?
 * - Payment payload tamper-proof banana ke liye
 * - Agar koi amount change karne ki koshish kare, signature verify fail hogi
 * - Non-repudiation: sender deny nahi kar sakta ki usne payment ki
 *
 * How it works:
 * 1. SIGN: Private key se payload ka SHA256 hash sign karo
 * 2. VERIFY: Public key se signature verify karo
 *
 * Key Management (Production mein):
 * - Private key: Secure vault (AWS KMS, HashiCorp Vault)
 * - Public key: Config server se
 * - Keys rotate karo regularly
 *
 * Key Generation:
 * openssl genrsa -out private.pem 2048
 * openssl rsa -in private.pem -pubout -out public.pem
 * ================================================================
 */

@Component
@Slf4j
public class PaymentSignatureUtil {

    @Value("${payment.rsa.private-key}")
    private String privateKeyBase64;

    @Value("${payment.rsa.public-key}")
    private String publicKeyBase64;

    private static final String ALGORITHM = "SHA256withRSA";

    /**
     * Payload ko RSA private key se sign karo
     * Return: Base64 encoded signature string
     */
    public String sign(String payload) {
        try{
            PrivateKey privateKey = loadPrivateKey();
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initSign(privateKey);
            signature.update(payload.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        }catch (Exception e){
            log.error("Failed to sign payment payload: {}", e.getMessage());
            throw new RuntimeException("Payment signing failed", e);
        }
    }

    /**
     * RSA public key se signature verify karo
     */
    public boolean verify(String payload, String signatureBase64){
        try{
            PublicKey publicKey = (PublicKey) loadPrivateKey();
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(payload.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    private PrivateKey loadPrivateKey() throws Exception {
        String cleanKey = privateKeyBase64
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    private PublicKey loadPublicKey() throws Exception {
        String cleanKey = publicKeyBase64
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    /**
     * AES ke saath payment payload encrypt karo
     * Sensitive data (description, merchant info) ko encrypt karte hain
     */

    public String encryptPayload(String payload, String aesKeyBase64){

        try{
            byte[] keyBytes = Base64.getDecoder().decode(aesKeyBase64);
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            javax.crypto.spec.GCMParameterSpec parameterSpec =
                    new javax.crypto.spec.GCMParameterSpec(128, iv);

            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            byte[] encryptedData = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // IV + encrypted data ko combine karo
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            return Base64.getEncoder().encodeToString(combined);
        }catch (Exception e){
            throw new RuntimeException("Payload encryption failed", e);
        }
    }
}
