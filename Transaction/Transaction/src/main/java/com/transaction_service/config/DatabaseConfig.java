package com.transaction_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * ================================================================
 * Dual Database Configuration
 * ================================================================
 * Spring Boot by default sirf ek database autowire karta hai.
 * Yahan explicitly batate hain ki:
 *   - JPA Repositories: repository.mysql package mein hain (MySQL)
 *   - Mongo Repositories: repository.mongo package mein hain (MongoDB)
 *
 * application.yml mein dono ke connection details hain.
 * ================================================================
 */
@Configuration
@EnableJpaRepositories(
        basePackages ="com.transaction_service.repository.mysql"
)
@EnableMongoRepositories(
        basePackages = "com.transaction_service.repository.mongo"
)
public class DatabaseConfig {
    // Spring Boot auto-configures datasource beans from application.yml
    // Sirf scan packages explicitly specify karne hain
}

