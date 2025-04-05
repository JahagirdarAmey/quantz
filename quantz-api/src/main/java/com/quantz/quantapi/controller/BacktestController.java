package com.quantz.quantapi.controller;

import com.quantz.quantapi.dto.BacktestRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/backtest")
public class BacktestController {

    @PostMapping
    public ResponseEntity<Map<String, Object>> runBacktest(@RequestBody BacktestRequest request) {
        // Simulate a backtest response (replace with actual implementation later)
        String backtestId = UUID.randomUUID().toString();

        return ResponseEntity.ok(Map.of(
                "id", backtestId,
                "status", "completed",
                "strategyType", request.getStrategyType(),
                "instruments", request.getInstruments(),
                "startDate", request.getStartDate(),
                "endDate", request.getEndDate(),
                "parameters", request.getParameters(),
                "results", Map.of(
                        "initialCapital", 10000,
                        "finalCapital", 12500,
                        "totalReturn", 25.0,
                        "annualizedReturn", 15.7,
                        "sharpeRatio", 1.2,
                        "maxDrawdown", 5.8,
                        "trades", 42,
                        "winRate", 65.0
                ),
                "createdAt", Instant.now().toString()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBacktestResult(@PathVariable String id) {
        // Simulate fetching a backtest result (replace with actual implementation later)
        return ResponseEntity.ok(Map.of(
                "id", id,
                "status", "completed",
                "strategyType", "MovingAverageCrossover",
                "instruments", List.of("TATAMOTORS.NS", "RELIANCE.NS"),
                "startDate", "2023-01-01",
                "endDate", "2023-12-31",
                "parameters", Map.of(
                        "shortPeriod", 20,
                        "longPeriod", 50
                ),
                "results", Map.of(
                        "initialCapital", 10000,
                        "finalCapital", 12500,
                        "totalReturn", 25.0,
                        "annualizedReturn", 15.7,
                        "sharpeRatio", 1.2,
                        "maxDrawdown", 5.8,
                        "trades", 42,
                        "winRate", 65.0
                ),
                "createdAt", Instant.now().minus(java.time.Duration.ofDays(1)).toString()
        ));
    }
}