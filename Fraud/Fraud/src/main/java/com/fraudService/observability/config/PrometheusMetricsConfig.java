package com.fraudService.observability.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Micrometer Prometheus metric instrumentation for SentinelX / UPI Mesh.
 * Registers meters for throughput, Kafka lag, risk/fraud/ML/DB/Redis latencies, API errors, and service health.
 */
@Configuration
public class PrometheusMetricsConfig {

    private final AtomicInteger serviceHealthGauge = new AtomicInteger(1);
    private final AtomicInteger kafkaConsumerLagGauge = new AtomicInteger(0);

    @Bean
    public Counter transactionThroughputCounter(MeterRegistry registry) {
        return Counter.builder("transaction_throughput_total")
                .description("Total number of UPI payment transactions processed")
                .tag("service", "fraud-service")
                .register(registry);
    }

    @Bean
    public Counter apiErrorsCounter(MeterRegistry registry) {
        return Counter.builder("api_errors_total")
                .description("Total number of API HTTP 4xx and 5xx errors")
                .tag("service", "fraud-service")
                .register(registry);
    }

    @Bean
    public Timer riskEvaluationTimer(MeterRegistry registry) {
        return Timer.builder("risk_evaluation_duration_seconds")
                .description("Latency distribution of risk scoring evaluation engine")
                .tag("service", "fraud-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    @Bean
    public Timer fraudDetectionTimer(MeterRegistry registry) {
        return Timer.builder("fraud_detection_duration_seconds")
                .description("Latency distribution of complete fraud detection pipeline")
                .tag("service", "fraud-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    @Bean
    public Timer mlPredictionTimer(MeterRegistry registry) {
        return Timer.builder("ml_prediction_duration_seconds")
                .description("Latency distribution of Python ML service predictions")
                .tag("service", "fraud-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    @Bean
    public Timer databaseQueryTimer(MeterRegistry registry) {
        return Timer.builder("database_query_duration_seconds")
                .description("Latency distribution of MongoDB / MySQL queries")
                .tag("service", "fraud-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    @Bean
    public Timer redisCommandTimer(MeterRegistry registry) {
        return Timer.builder("redis_command_duration_seconds")
                .description("Latency distribution of Redis cache and velocity operations")
                .tag("service", "fraud-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    @Bean
    public Gauge serviceHealthGauge(MeterRegistry registry) {
        return Gauge.builder("service_health_status", serviceHealthGauge, AtomicInteger::get)
                .description("Health status of microservice (1 = UP, 0 = DOWN)")
                .tag("service", "fraud-service")
                .register(registry);
    }

    @Bean
    public Gauge kafkaConsumerLagGauge(MeterRegistry registry) {
        return Gauge.builder("kafka_consumer_lag_records", kafkaConsumerLagGauge, AtomicInteger::get)
                .description("Kafka partition consumer record lag")
                .tag("service", "fraud-service")
                .register(registry);
    }
}
