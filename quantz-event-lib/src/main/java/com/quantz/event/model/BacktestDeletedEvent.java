package com.quantz.event.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Event triggered when a backtest is deleted
 */
public record BacktestDeletedEvent(
        UUID backtestId,
        UUID userId
) {
    public BacktestDeletedEvent {
        Objects.requireNonNull(backtestId, "backtestId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
    }
}