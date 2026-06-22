package com.upimesh.bankgateway.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@UtilityClass
@Slf4j
public class AccountEncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;
    private static final String KEY_PREFIX = "ENC:";

    private static final SecretKey DEV_SECRET_KEY = generateDevKey();

    private static SecretKey generateDevKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256, new SecureRandom());
            log.warn("⚠️ Using generated AES-256 key for AccountEncryptionUtil. Use KMS/Vault in production!");
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }

    public static String encrypt(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[GCM_IV_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, DEV_SECRET_KEY,
                    new GCMParameterSpec(GCM_TAG_BITS, iv));

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            buffer.put(iv);
            buffer.put(ciphertext);

            return KEY_PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static String decrypt(String encryptedValue) {
        if (encryptedValue == null) return null;
        if (!encryptedValue.startsWith(KEY_PREFIX)) {
            return encryptedValue;
        }

        try {
            byte[] combined = Base64.getDecoder().decode(encryptedValue.substring(KEY_PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[GCM_IV_BYTES];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, DEV_SECRET_KEY,
                    new GCMParameterSpec(GCM_TAG_BITS, iv));

            return new String(cipher.doFinal(ciphertext));
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed — data may be corrupted", e);
        }
    }

    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(KEY_PREFIX);
    }

    public static String mask(String accountNumber) {
        if (accountNumber == null) return null;
        if (accountNumber.length() <= 4) {
            return "XXXX XXXX " + accountNumber;
        }
        return "XXXX XXXX " + accountNumber.substring(accountNumber.length() - 4);
    }

    public static String safeLog(String value) {
        if (value == null) return null;
        if (isEncrypted(value)) {
            return "[ENCRYPTED]";
        }
        return mask(value);
    }
}
