package com.quantz.event.model;

import java.util.Map;
import java.util.UUID;

public record BacktestCompletedEvent(
        UUID backtestId,
        UUID userId,
        String status,
        Map<String, Double> metrics
) {
}