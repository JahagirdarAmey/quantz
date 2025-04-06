package com.quantz.event.model;

import java.util.UUID;

public record SimulationStartedEvent(
        UUID backtestId
) {
}