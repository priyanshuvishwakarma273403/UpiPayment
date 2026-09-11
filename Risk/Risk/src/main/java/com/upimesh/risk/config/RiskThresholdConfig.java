package com.upimesh.risk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "risk.thresholds")
@Data
public class RiskThresholdConfig {

    private double allowThreshold = 20.0;
    private double monitorThreshold = 40.0;
    private double stepUpThreshold = 60.0;
    private double reviewThreshold = 80.0;
}
