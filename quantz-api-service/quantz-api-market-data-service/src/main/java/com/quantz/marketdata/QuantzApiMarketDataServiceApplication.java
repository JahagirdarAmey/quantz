package com.quantz.marketdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
@EnableRetry
@EnableAsync
public class QuantzApiMarketDataServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuantzApiMarketDataServiceApplication.class, args);
    }
}