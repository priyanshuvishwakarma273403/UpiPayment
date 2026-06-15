package com.npci.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * EncryptionUtil — AES-256-GCM encryption for sensitive payment data.
 *
 * Used to encrypt:
 * - MPIN hashes before storage (extra layer)
 * - Sensitive payload fields before logging
 * - Any PII (Personally Identifiable Information)
 *
 * AES-256-GCM chosen because:
 * - 256-bit key = quantum-resistant for now
 * - GCM mode = authenticated encryption (detects tampering)
 * - Industry standard for PCI-DSS compliance
 *
 * NOTE: In production, use AWS KMS or HashiCorp Vault for key management.
 * Never hardcode encryption keys.
 */

@UtilityClass
@Slf4j
public class EncryptionUtil {

    private static final String ALGORITHM     = "AES/GCM/NoPadding";
    private static final int    GCM_TAG_BITS  = 128;
    private static final int    GCM_IV_BYTES  = 12;
    private static final String KEY_PREFIX    = "ENC:";

    // In production: load from Vault/KMS. Here: generated at startup for dev.
    private static final SecretKey DEV_SECRET_KEY = generateDevKey();

    private static SecretKey generateDevKey(){

        try{
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256, new SecureRandom());
            log.warn("⚠️  Using generated AES-256 key. Use KMS/Vault in production!");
            return keyGen.generateKey();
        }catch(Exception e){
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }

    /**
     * Encrypt plaintext using AES-256-GCM.
     * Returns "ENC:" prefix + Base64(IV + ciphertext + auth tag)
     */
    public static String encrypt(String plaintext){
        if(plaintext == null) return null;
        try{
            byte[] iv = new byte[GCM_IV_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, DEV_SECRET_KEY,
                    new GCMParameterSpec(GCM_IV_BYTES, iv));

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            // Prepend IV to ciphertext (needed for decryption)
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            buffer.put(iv);
            buffer.put(ciphertext);

            return KEY_PREFIX + Base64.getEncoder().encodeToString(buffer.array());

        }catch (Exception e){
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }


}
