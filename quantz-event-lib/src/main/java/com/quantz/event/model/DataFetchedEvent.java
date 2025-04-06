package com.quantz.event.model;

import com.quantz.quantcommon.model.MarketData;

import java.util.List;
import java.util.UUID;

public record DataFetchedEvent(
        UUID backtestId,
        List<MarketData> marketData
) {
}