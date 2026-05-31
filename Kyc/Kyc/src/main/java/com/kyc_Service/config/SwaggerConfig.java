package com.kyc_Service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ================================================================
 * Swagger / OpenAPI Configuration - KYC Service
 * ================================================================
 * Swagger UI: http://localhost:8091/swagger-ui.html
 * API Docs:   http://localhost:8091/v3/api-docs
 * ================================================================
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UPI Mesh - KYC Service API")
                        .description("""
                                RBI-compliant Know Your Customer (KYC) service.

                                Features:
                                - Aadhaar e-KYC (UIDAI OTP-based verification)
                                - PAN card verification (NSDL database)
                                - Face match (selfie vs Aadhaar photo, 80% threshold)
                                - 3-level KYC: Level 0 (₹10K limit) -> Level 1 (₹1L) -> Level 2 (Unlimited)
                                - KYC expiry tracking (10-year validity per RBI guideline)

                                Mock Mode:
                                Set kyc.mock-mode=true in application.yml for development.
                                Real APIs: UIDAI, NSDL, HyperVerge/AWS Rekognition.
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("UPI Mesh Team")
                                .email("dev@upimesh.com"))
                        .license(new License()
                                .name("Private - UPI Mesh")
                                .url("https://upimesh.com")));
    }
}