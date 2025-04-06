package com.quantz.event.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record BacktestCreatedEvent(
        UUID backtestId,
        UUID userId,
        UUID strategyId,
        List<String> instruments,
        LocalDate startDate,
        LocalDate endDate,
        String dataInterval,
        @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.DecimalMin("0") Float initialCapital,
        Map<String, Object> strategyConfig
) {
    public BacktestCreatedEvent {
        Objects.requireNonNull(backtestId, "backtestId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
    }
}





