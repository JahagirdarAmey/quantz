package com.quantz.backtest.mapper;

import com.quantz.model.BacktestCreationResponse;
import com.quantz.model.BacktestRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.OffsetDateTime;
import java.util.Optional; // Add this import
import java.util.UUID;

/**
 * Mapper for creating response objects
 */
@Mapper(componentModel = "spring", imports = {Optional.class})
public interface BacktestResponseMapper {

    /**
     * Create a BacktestCreationResponse from a backtestId and estimatedCompletionTime
     */
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "estimatedCompletionTime", expression = "java(Optional.ofNullable(estimatedCompletionTime))")
    BacktestCreationResponse toCreationResponse(UUID backtestId, OffsetDateTime estimatedCompletionTime);

    // Converting OffsetDateTime to Optional<OffsetDateTime>
    default Optional<OffsetDateTime> map(OffsetDateTime value) {
        return Optional.ofNullable(value);
    }
}