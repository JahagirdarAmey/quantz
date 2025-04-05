package com.quantz.quantapi.dto;

import com.quantz.quantcommon.model.MarketData;
import com.yourdomain.quantcommon.model.Signal;
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
    
    @NotBlank(message = "Strategy ID is required")
    private String id;
    
    @NotBlank(message = "Strategy name is required")
    private String name;
    
    @NotBlank(message = "Strategy type is required")
    private String type;
    
    private String description;
    
    @NotNull(message = "Initial capital is required")
    @Positive(message = "Initial capital must be positive")
    private BigDecimal initialCapital;
    
    @NotNull(message = "Risk per trade is required")
    @Positive(message = "Risk per trade must be positive")
    private BigDecimal riskPerTrade;
    
    private Map<String, Object> parameters;
    
    private boolean active;
    
    // Method to be implemented by concrete strategy implementations
    public List<Signal> generateSignals(List<MarketData> marketDataList) {
        throw new UnsupportedOperationException("Method should be implemented by concrete strategy classes");
    }
}