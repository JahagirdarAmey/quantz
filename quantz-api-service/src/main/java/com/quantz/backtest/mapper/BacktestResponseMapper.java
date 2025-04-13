package com.quantz.backtest.mapper;

import com.quantz.backtest.model.BacktestCreationResponse;
import com.quantz.backtest.model.BacktestRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Mapper for creating response objects
 */
@Mapper(componentModel = "spring")
public interface BacktestResponseMapper {
    
    /**
     * Create a BacktestCreationResponse from a backtestId and estimatedCompletionTime
     */
    @Mapping(target = "status", constant = "PENDING")
    BacktestCreationResponse toCreationResponse(UUID backtestId, OffsetDateTime estimatedCompletionTime);
}