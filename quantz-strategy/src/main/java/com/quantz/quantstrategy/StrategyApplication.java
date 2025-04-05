package com.quantz.quantstrategy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.quantz.quantstrategy", "com.quantz.quantcommon"})
public class StrategyApplication {

    public static void main(String[] args) {
        SpringApplication.run(StrategyApplication.class, args);
    }
}
