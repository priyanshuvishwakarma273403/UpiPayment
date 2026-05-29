package com.paymentService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Payment Service - Main Application
 *
// * @EnableFeignClients  -> wallet-service aur fraud-service ko call karta hai
// * @EnableKafka         -> Kafka producer/consumer enable karta hai
// * @EnableDiscoveryClient -> Eureka mein register hoga
 */


@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableKafka
public class PaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentApplication.class, args);
	}

}
