package com.quantz.backtest.mapper;

import com.quantz.backtest.entity.BacktestEntity;
import com.quantz.backtest.model.BacktestSummary;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * MapStruct mapper for converting between BacktestEntity and BacktestSummary
 */
@Mapper(componentModel = "spring", 
        imports = {Arrays.class, UUID.class, Optional.class, LocalDateTime.class, 
                  OffsetDateTime.class, ZoneOffset.class, Collections.class})
public interface BacktestSummaryMapper {
    
    /**
     * Convert BacktestEntity to BacktestSummary
     * 
     * @param entity the backtest entity
     * @return the backtest summary
     */
    @Mapping(target = "backtestId", expression = "java(UUID.fromString(entity.getId()))")
    @Mapping(target = "strategyId", expression = "java(UUID.fromString(entity.getStrategyId()))")
    @Mapping(target = "status", expression = "java(mapStatus(entity.getStatus()))")
    @Mapping(target = "name", expression = "java(Optional.ofNullable(entity.getName()))")
    @Mapping(target = "strategyName", expression = "java(Optional.empty())")  // Needs to be populated later
    @Mapping(target = "createdAt", expression = "java(toOffsetDateTime(entity.getCreatedAt()))")
    @Mapping(target = "completedAt", expression = "java(entity.getCompletedAt() != null ? Optional.of(toOffsetDateTime(entity.getCompletedAt())) : Optional.empty())")
    @Mapping(target = "startDate", expression = "java(Optional.ofNullable(entity.getStartDate()))")
    @Mapping(target = "endDate", expression = "java(Optional.ofNullable(entity.getEndDate()))")
    @Mapping(target = "instruments", expression = "java(parseInstruments(entity.getInstruments()))")
    BacktestSummary toBacktestSummary(BacktestEntity entity);
    
    /**
     * Convert a list of BacktestEntity to a list of BacktestSummary
     * 
     * @param entities the list of backtest entities
     * @return the list of backtest summaries
     */
    List<BacktestSummary> toBacktestSummaryList(List<BacktestEntity> entities);
    
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
    default BacktestSummary.StatusEnum mapStatus(String status) {
        if (status == null) {
            return null;
        }
        
        try {
            return BacktestSummary.StatusEnum.fromValue(status.toLowerCase());
        } catch (IllegalArgumentException e) {
            // Default to PENDING if status is not recognized
            return BacktestSummary.StatusEnum.PENDING;
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
}