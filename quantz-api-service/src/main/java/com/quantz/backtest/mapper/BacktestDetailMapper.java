package com.quantz.backtest.mapper;

import com.quantz.backtest.entity.BacktestEntity;
import com.quantz.backtest.model.BacktestDetail;
import com.quantz.backtest.model.PerformanceMetricsSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * MapStruct mapper for converting between BacktestEntity and BacktestDetail
 */
@Mapper(componentModel = "spring", 
        imports = {Arrays.class, UUID.class, Optional.class, LocalDateTime.class, 
                  OffsetDateTime.class, ZoneOffset.class, Collections.class})
public interface BacktestDetailMapper {
    
    /**
     * Convert BacktestEntity to BacktestDetail
     * 
     * @param entity the backtest entity
     * @return the backtest detail
     */
    @Mapping(target = "backtestId", expression = "java(UUID.fromString(entity.getId()))")
    @Mapping(target = "strategyId", expression = "java(UUID.fromString(entity.getStrategyId()))")
    @Mapping(target = "status", expression = "java(mapStatus(entity.getStatus()))")
    @Mapping(target = "name", expression = "java(Optional.ofNullable(entity.getName()))")
    @Mapping(target = "strategyName", expression = "java(Optional.empty())")  // Need to be populated separately if needed
    @Mapping(target = "createdAt", expression = "java(toOffsetDateTime(entity.getCreatedAt()))")
    @Mapping(target = "completedAt", expression = "java(entity.getCompletedAt() != null ? Optional.of(toOffsetDateTime(entity.getCompletedAt())) : Optional.empty())")
    @Mapping(target = "startDate", expression = "java(Optional.ofNullable(entity.getStartDate()))")
    @Mapping(target = "endDate", expression = "java(Optional.ofNullable(entity.getEndDate()))")
    @Mapping(target = "instruments", expression = "java(parseInstruments(entity.getInstruments()))")
    @Mapping(target = "metrics", expression = "java(parseMetrics(entity.getResults()))")
    @Mapping(target = "strategyConfig", expression = "java(parseStrategyConfig(entity.getStrategyConfig()))")
    @Mapping(target = "initialCapital", expression = "java(Optional.ofNullable(entity.getInitialCapital()))")
    @Mapping(target = "commission", expression = "java(Optional.ofNullable(entity.getCommission()))")
    @Mapping(target = "slippage", expression = "java(Optional.ofNullable(entity.getSlippage()))")
    @Mapping(target = "dataInterval", expression = "java(mapDataInterval(entity.getDataInterval()))")
    @Mapping(target = "errorMessage", expression = "java(Optional.ofNullable(entity.getErrorMessage()))")
    BacktestDetail toBacktestDetail(BacktestEntity entity);
    
    /**
     * Convert LocalDateTime to OffsetDateTime
     */
    default OffsetDateTime toOffsetDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atOffset(ZoneOffset.UTC);
    }
    
    /**
     * Map status string to StatusEnum
     */
    default BacktestDetail.StatusEnum mapStatus(String status) {
        if (status == null) {
            return null;
        }
        
        try {
            return BacktestDetail.StatusEnum.fromValue(status.toLowerCase());
        } catch (IllegalArgumentException e) {
            // Default to PENDING if status is not recognized
            return BacktestDetail.StatusEnum.PENDING;
        }
    }
    
    /**
     * Map data interval string to DataIntervalEnum
     */
    default Optional<BacktestDetail.DataIntervalEnum> mapDataInterval(String dataInterval) {
        if (dataInterval == null || dataInterval.isEmpty()) {
            return Optional.empty();
        }
        
        try {
            return Optional.of(BacktestDetail.DataIntervalEnum.fromValue(dataInterval));
        } catch (IllegalArgumentException e) {
            // Return empty if interval is not recognized
            return Optional.empty();
        }
    }
    
    /**
     * Convert a comma-separated string of instruments to a List
     */
    default List<String> parseInstruments(String instruments) {
        if (instruments == null || instruments.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(instruments.split(","));
    }
    
    /**
     * Parse metrics from JSON string
     */
    default Optional<PerformanceMetricsSummary> parseMetrics(String resultsJson) {
        if (resultsJson == null || resultsJson.isEmpty()) {
            return Optional.empty();
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PerformanceMetricsSummary metrics = objectMapper.readValue(resultsJson, PerformanceMetricsSummary.class);
            return Optional.of(metrics);
        } catch (JsonProcessingException e) {
            // Return empty if there's an error parsing the JSON
            return Optional.empty();
        }
    }
    
    /**
     * Parse strategy config from JSON string
     */
    @SuppressWarnings("unchecked")
    default Map<String, Object> parseStrategyConfig(String strategyConfigJson) {
        if (strategyConfigJson == null || strategyConfigJson.isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(strategyConfigJson, Map.class);
        } catch (JsonProcessingException e) {
            // Return empty map if there's an error parsing the JSON
            return new HashMap<>();
        }
    }
}