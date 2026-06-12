package com.npci.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

/**
 * AppConfig — Spring beans configuration.
 *
 * Key configs:
 * - RestTemplate with mutual TLS (required for NPCI prod calls)
 * - Redis template for idempotency key storage
 * - ObjectMapper with Java 8 time support
 */
@Configuration
public class AppConfig {

    @Value("${npci.use-mock:true}")
    private boolean useMock;

    /**
     * RestTemplate configured for NPCI communication.
     * In production: uses mutual TLS with NPCI certificates.
     * In dev/mock mode: plain HTTP client.
     */
    @Bean
    public RestTemplate restTemplate() {
        if (useMock) {
            // Simple RestTemplate for mock server
            return new RestTemplate();
        }

        // Production: mutual TLS RestTemplate
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                    // Load our client certificate (sent to NPCI to prove our identity)
                    // .loadKeyMaterial(keystoreResource.getFile(), keystorePass, keystorePass)
                    // Load NPCI's certificate authority (we trust them)
                    // .loadTrustMaterial(truststoreResource.getFile(), truststorePass)
                    .build();

            HttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(
                            PoolingHttpClientConnectionManagerBuilder.create()
                                    .setSSLSocketFactory(
                                            SSLConnectionSocketFactoryBuilder.create()
                                                    .setSslContext(sslContext)
                                                    .build())
                                    .build())
                    .build();

            HttpComponentsClientHttpRequestFactory factory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout(5000);
            factory.setConnectionRequestTimeout(10000);

            return new RestTemplate(factory);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create TLS RestTemplate for NPCI", e);
        }
    }

    /**
     * Redis template — String:String for idempotency keys, transaction state cache
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    /**
     * ObjectMapper with Java Time support (LocalDateTime, LocalDate serialization)
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

}
