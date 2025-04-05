package com.quantz.quantstrategy.strategy;

import com.quantz.quantcommon.model.MarketData;
import com.quantz.quantcommon.model.Signal;

import java.util.List;
import java.util.Map;

/**
 * Interface for all trading strategies
 */
public interface Strategy {
    
    /**
     * Generate trading signals based on market data
     * 
     * @param marketDataList List of market data points
     * @return List of signals generated
     */
    List<Signal> generateSignals(List<MarketData> marketDataList);
    
    /**
     * Get the strategy type
     * 
     * @return The strategy type
     */
    String getType();
    
    /**
     * Set strategy parameters
     * 
     * @param parameters Map of parameter name to value
     */
    void setParameters(Map<String, Object> parameters);
}