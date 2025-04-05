package com.quantz.quantapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;
import java.util.Map;

/**
 * DTO for backtesting request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestRequest {
    
    private String strategyType;
    
    private List<String> instruments;
    
    private String startDate;
    
    private String endDate;
    
    private Map<String, Object> parameters;
    
    private String timeframe;
    
    private boolean includeFees;
}