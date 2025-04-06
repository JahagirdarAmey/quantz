package com.quantz.event.model;

import java.util.UUID;

public record SimulationCompletedEvent(
        UUID backtestId,
        String status,
        SimulationResults results,
        String errorMessage
) {
}