package com.quantz.backtest.mapper;

import com.quantz.backtest.model.BacktestRequest;
import com.quantz.backtest.entity.BacktestEntity;
import com.quantz.event.model.BacktestCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * MapStruct mapper for converting between BacktestRequest DTO and entity/event objects
 */
@Mapper(componentModel = "spring", imports = {Arrays.class, LocalDateTime.class, UUID.class})
public interface BacktestMapper {

    /**
     * Convert BacktestRequest to BacktestEntity
     *
     * @param request the backtest request DTO
     * @param backtestId the generated UUID for the backtest
     * @param userId the current user ID
     * @return the created BacktestEntity
     */
    @Mapping(target = "id", expression = "java(backtestId.toString())")
    @Mapping(target = "userId", expression = "java(userId.toString())")
    @Mapping(target = "strategyId", expression = "java(request.getStrategyId().toString())")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "instruments", expression = "java(String.join(\",\", request.getInstruments()))")
    @Mapping(target = "name", expression = "java(request.getName().orElse(\"Backtest \" + backtestId.toString().substring(0, 8)))")
    @Mapping(target = "commission", expression = "java(request.getCommission().orElse(0.001f))")
    @Mapping(target = "slippage", expression = "java(request.getSlippage().orElse(0.0005f))")
    @Mapping(target = "dataInterval", expression = "java(request.getDataInterval().map(BacktestRequest.DataIntervalEnum::getValue).orElse(\"1d\"))")
    @Mapping(target = "strategyConfig", expression = "java(convertStrategyConfigToJson(request.getStrategyConfig()))")
    @Mapping(target = "results", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    BacktestEntity toEntity(BacktestRequest request, UUID backtestId, UUID userId);

    /**
     * Convert BacktestRequest to BacktestCreatedEvent
     *
     * @param request the backtest request DTO
     * @param backtestId the generated UUID for the backtest
     * @param userId the current user ID
     * @return the created BacktestCreatedEvent
     */
    @Mapping(target = "dataInterval", expression = "java(request.getDataInterval().map(interval -> interval.getValue()).orElse(\"1d\"))")
    BacktestCreatedEvent toEvent(BacktestRequest request, UUID backtestId, UUID userId);

    /**
     * Convert BacktestEntity to BacktestCreatedEvent
     *
     * @param entity the backtest entity
     * @return the created BacktestCreatedEvent
     */
    @Mapping(target = "backtestId", expression = "java(UUID.fromString(entity.getId()))")
    @Mapping(target = "userId", expression = "java(UUID.fromString(entity.getUserId()))")
    @Mapping(target = "strategyId", expression = "java(UUID.fromString(entity.getStrategyId()))")
    @Mapping(target = "instruments", expression = "java(parseInstruments(entity.getInstruments()))")
    @Mapping(target = "strategyConfig", expression = "java(parseStrategyConfig(entity.getStrategyConfig()))")
    BacktestCreatedEvent entityToEvent(BacktestEntity entity);

    /**
     * Convert a comma-separated string of instruments to a List
     */
    default List<String> parseInstruments(String instruments) {
        if (instruments == null || instruments.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return Arrays.asList(instruments.split(","));
    }

    /**
     * Convert a Map to a JSON string
     */
    default String convertStrategyConfigToJson(java.util.Map<String, Object> strategyConfig) {
        if (strategyConfig == null || strategyConfig.isEmpty()) {
            return null;
        }

        try {
            return new ObjectMapper().writeValueAsString(strategyConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize strategy config", e);
        }
    }

    /**
     * Convert a JSON string to a Map
     */
    @SuppressWarnings("unchecked")
    default java.util.Map<String, Object> parseStrategyConfig(String strategyConfigJson) {
        if (strategyConfigJson == null || strategyConfigJson.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        try {
            return new ObjectMapper().readValue(strategyConfigJson, java.util.Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize strategy config", e);
        }
    }
}