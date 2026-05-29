package com.transaction_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Transaction Service - Main Application
 *
 * Dual database:
 * - MySQL  : Transaction ledger (structured, queryable)
 * - MongoDB: Audit logs, payment logs (flexible, high write volume)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class TransactionApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionApplication.class, args);
	}

}
