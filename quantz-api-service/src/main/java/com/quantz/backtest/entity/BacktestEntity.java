package com.quantz.backtest.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
// Other imports

@Entity
@Table(name = "backtests")
@Getter
@Setter
public class BacktestEntity {
    @Id
    private String id;

    private String userId;
    private String name;
    private String strategyId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDate startDate;
    private LocalDate endDate;

    private String Instruments;
    private Float InitialCapital;
    private Double Commission;
    private Double Slippage;
    private String DataInterval;

    private String strategyConfig;


}