package com.quantz.quantstrategy.strategy;

import com.quantz.quantcommon.model.MarketData;
import com.quantz.quantcommon.model.OrderSide;
import com.quantz.quantcommon.model.Signal;
import com.quantz.quantcommon.model.SignalType;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of a Breakout strategy.
 * 
 * Generates BUY signals when price breaks above a resistance level,
 * and SELL signals when price breaks below a support level.
 */
@Slf4j
@NoArgsConstructor
public class BreakoutStrategy implements Strategy {

    private Map<String, Object> parameters = new HashMap<>();
    
    // Default parameter values
    private int lookbackPeriod = 20;
    private double volumeThreshold = 1.5;
    private boolean useVolume = true;
    
    public BreakoutStrategy(Map<String, Object> parameters) {
        setParameters(parameters);
    }
    
    @Override
    public List<Signal> generateSignals(List<MarketData> marketDataList) {
        List<Signal> signals = new ArrayList<>();
        
        if (marketDataList == null || marketDataList.size() < lookbackPeriod + 1) {
            return signals; // Not enough data
        }
        
        // Apply parameters
        applyParameters();
        
        // Loop through data, starting after lookback period
        for (int i = lookbackPeriod; i < marketDataList.size(); i++) {
            MarketData currentData = marketDataList.get(i);
            List<MarketData> lookbackData = marketDataList.subList(i - lookbackPeriod, i);
            
            // Find highest high and lowest low in lookback period
            BigDecimal resistance = findHighest(lookbackData);
            BigDecimal support = findLowest(lookbackData);
            
            // Check volume condition if enabled
            boolean volumeCondition = !useVolume || 
                    (currentData.getVolume() > getAverageVolume(lookbackData) * volumeThreshold);
            
            // Check for breakout conditions
            if (volumeCondition) {
                // Bullish breakout
                if (currentData.getClose().compareTo(resistance) > 0) {
                    signals.add(createSignal(currentData, SignalType.ENTRY, OrderSide.BUY, resistance, support));
                }
                // Bearish breakout
                else if (currentData.getClose().compareTo(support) < 0) {
                    signals.add(createSignal(currentData, SignalType.ENTRY, OrderSide.SELL, resistance, support));
                }
            }
        }
        
        return signals;
    }
    
    @Override
    public String getType() {
        return "BreakoutStrategy";
    }
    
    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    private void applyParameters() {
        if (parameters != null) {
            if (parameters.containsKey("lookbackPeriod")) {
                lookbackPeriod = ((Number) parameters.get("lookbackPeriod")).intValue();
            }
            
            if (parameters.containsKey("volumeThreshold")) {
                volumeThreshold = ((Number) parameters.get("volumeThreshold")).doubleValue();
            }
            
            if (parameters.containsKey("useVolume")) {
                useVolume = (Boolean) parameters.get("useVolume");
            }
        }
    }
    
    private BigDecimal findHighest(List<MarketData> dataList) {
        return dataList.stream()
                .map(MarketData::getHigh)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
    
    private BigDecimal findLowest(List<MarketData> dataList) {
        return dataList.stream()
                .map(MarketData::getLow)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
    
    private double getAverageVolume(List<MarketData> dataList) {
        return dataList.stream()
                .mapToLong(MarketData::getVolume)
                .average()
                .orElse(0);
    }
    
    private Signal createSignal(MarketData marketData, SignalType type, OrderSide side, 
                               BigDecimal resistance, BigDecimal support) {
        BigDecimal stopLoss;
        BigDecimal takeProfit;
        
        if (side == OrderSide.BUY) {
            stopLoss = support;
            takeProfit = marketData.getClose().add(
                    marketData.getClose().subtract(stopLoss).multiply(BigDecimal.valueOf(2)));
        } else {
            stopLoss = resistance;
            takeProfit = marketData.getClose().subtract(
                    stopLoss.subtract(marketData.getClose()).multiply(BigDecimal.valueOf(2)));
        }
        
        return Signal.builder()
                .strategyId(getType())
                .type(type)
                .side(side)
                .symbol(marketData.getSymbol())
                .instrumentToken(marketData.getInstrumentToken())
                .price(marketData.getLastPrice())
                .stopLoss(stopLoss)
                .takeProfit(takeProfit)
                .timestamp(Instant.now())
                .metadata(Map.of(
                        "lookbackPeriod", lookbackPeriod,
                        "resistance", resistance,
                        "support", support,
                        "volume", marketData.getVolume(),
                        "volumeThreshold", volumeThreshold
                ))
                .build();
    }
}