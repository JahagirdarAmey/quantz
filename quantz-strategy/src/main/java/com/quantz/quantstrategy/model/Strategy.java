package com.quantz.quantstrategy.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Represents a trading strategy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Strategy {
    
    private String id;
    
    private String name;
    
    private String type;
    
    private String description;
    
    private BigDecimal initialCapital;
    
    private BigDecimal riskPerTrade;
    
    private Map<String, Object> parameters;
    
    private boolean active;
    
    // Method to be implemented by concrete strategy implementations
    public List<Signal> generateSignals(List<MarketData> marketDataList) {
        throw new UnsupportedOperationException("Method should be implemented by concrete strategy classes");
    }
}