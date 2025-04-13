package com.quantz.backtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.quantz.backtest",
        "com.quantz.common",
        "com.quantz.event"
})
public class BacktestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BacktestServiceApplication.class, args);
    }
}
