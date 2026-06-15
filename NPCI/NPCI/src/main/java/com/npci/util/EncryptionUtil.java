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

    /**
     * Decrypt an encrypted value.
     * Input must start with "ENC:" prefix.
     */
    public static String decrypt(String encryptedValue){
        if(encryptedValue == null) return null;
        if(!encryptedValue.startsWith(KEY_PREFIX)){
            return encryptedValue;
        }

        try{
            byte[] combined = Base64.getDecoder().decode(encryptedValue.substring(KEY_PREFIX.length()));
            // Extract IV (first 12 bytes) and ciphertext (rest)
            ByteBuffer buffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[GCM_IV_BYTES];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, DEV_SECRET_KEY,
                    new GCMParameterSpec(GCM_TAG_BITS, iv));

            return new String(cipher.doFinal(ciphertext));
        }catch (Exception e){
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed — data may be corrupted", e);
        }
    }

    /**
     * Check if a value is already encrypted.
     */
    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(KEY_PREFIX);
    }

    /**
     * Mask a value for safe logging (never log sensitive data raw).
     * "1234567890" → "123****890"
     */
    public static String maskForLog(String value) {
        if (value == null || value.length() <= 6) return "****";
        return value.substring(0, 3) + "****" + value.substring(value.length() - 3);
    }

    /**
     * Hash MPIN with SHA-256 + salt (one-way — cannot reverse).
     * Used to compare MPIN without storing original.
     */
    public static String hashMpin(String mpin, String salt) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest((salt + mpin).getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("MPIN hashing failed", e);
        }
    }

}
