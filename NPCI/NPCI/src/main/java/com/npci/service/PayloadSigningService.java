package com.npci.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;

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

}
