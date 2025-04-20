package com.quantz.backtest.mapper;

import com.quantz.model.BacktestSummary;
import com.quantz.model.ListBacktests200Response;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Optional;

/**
 * MapStruct mapper for creating response objects for list operations
 */
@Mapper(componentModel = "spring", imports = {Optional.class})
public interface ListBacktestsResponseMapper {
    
    /**
     * Create a ListBacktests200Response from a list of BacktestSummary and pagination parameters
     * 
     * @param backtests the list of backtest summaries
     * @param totalCount the total count of backtests matching the filter
     * @param limit the limit used in the query
     * @param offset the offset used in the query
     * @return the response object
     */
    @Mapping(target = "total", expression = "java(Optional.ofNullable(totalCount))")
    @Mapping(target = "limit", expression = "java(Optional.ofNullable(limit))")
    @Mapping(target = "offset", expression = "java(Optional.ofNullable(offset))")
    ListBacktests200Response toListResponse(List<BacktestSummary> backtests, Integer totalCount, Integer limit, Integer offset);
}