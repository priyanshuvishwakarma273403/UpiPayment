package com.bank_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableDiscoveryClient   // Eureka registration
@EnableFeignClients      // Inter-service calls
@EnableCaching           // Caffeine cache for IFSC lookups
public class BankGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankGatewayApplication.class, args);
	}

}
