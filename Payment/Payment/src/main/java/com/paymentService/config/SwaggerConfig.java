package com.paymentService.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UPI Payment Mesh - Payment Service API")
                        .description("Core payment processing: UPI, QR, Offline payments with fraud detection")
                        .version("v1.0.0"));
    }
}
