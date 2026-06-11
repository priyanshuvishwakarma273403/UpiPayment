package com.npci.service;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * PayloadSigningService — Signs NPCI request payloads using RSA SHA-256.
 *
 * NPCI requires all API requests to be digitally signed with the
 * merchant's private key. This proves the request is genuine and
 * hasn't been tampered with in transit.
 *
 * In production: private key comes from HSM (Hardware Security Module)
 * In dev: use a generated test RSA key pair
 */

@Service
@Slf4j
public class PayloadSigningService {

    @Value("${npci.tls.keystore-path:classpath:certs/test-private-key.pem}")
    private String privateKeyPath;

    private PrivateKey privateKey;

    public void init(){
        // Register Bouncy Castle as JCE provider for strong crypto
        Security.addProvider(new BouncyCastleProvider());
        try{
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
            generator.initialize(2048, new SecureRandom());
            KeyPair keyPair = generator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            log.warn("⚠️  Using GENERATED test RSA key. Replace with real HSM key in production!");
        }catch (Exception e){
            log.error("Failed to initialize signing key", e);
            throw new RuntimeException("Payment signing initialization failed", e);
        }
    }

    /**
     * Sign a payload string using RSA SHA-256.
     * Returns Base64-encoded signature.
     *
     * NPCI verifies this signature using our registered public key.
     */
    public String sign(String payload){
        try{
            Signature signature = Signature.getInstance("SHA256withRSA", "BC");
            signature.initSign(privateKey);
            signature.update(payload.getBytes(StandardCharsets.UTF_8));
            byte[] signedBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signedBytes);
        }catch (Exception e){
            log.error("Failed to sign NPCI payload", e);
            throw new RuntimeException("Payment signature generation failed", e);
        }
    }

    /**
     * Verify NPCI's response signature (to ensure response is genuine)
     */
    public boolean verify(String payload, String signature, PublicKey npciPublicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA", "BC");
            sig.initVerify(npciPublicKey);
            sig.update(payload.getBytes(StandardCharsets.UTF_8));
            byte[] sigBytes = Base64.getDecoder().decode(signature);
            return sig.verify(sigBytes);
        } catch (Exception e) {
            log.error("NPCI response signature verification failed", e);
            return false;
        }
    }
}
