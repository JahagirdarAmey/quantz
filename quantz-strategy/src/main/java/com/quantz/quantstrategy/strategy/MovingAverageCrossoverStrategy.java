package com.quantz.quantstrategy.strategy;

import com.yourdomain.quantcommon.model.MarketData;
import com.yourdomain.quantcommon.model.OrderSide;
import com.yourdomain.quantcommon.model.Signal;
import com.yourdomain.quantcommon.model.SignalType;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of a Moving Average Crossover strategy.
 * 
 * Generates BUY signals when the short MA crosses above the long MA,
 * and SELL signals when the short MA crosses below the long MA.
 */
@Slf4j
@NoArgsConstructor
public class MovingAverageCrossoverStrategy implements Strategy {

    private Map<String, Object> parameters = new HashMap<>();
    
    // Default parameter values
    private int shortPeriod = 20;
    private int longPeriod = 50;
    
    public MovingAverageCrossoverStrategy(Map<String, Object> parameters) {
        setParameters(parameters);
    }
    
    @Override
    public List<Signal> generateSignals(List<MarketData> marketDataList) {
        List<Signal> signals = new ArrayList<>();
        
        if (marketDataList == null || marketDataList.size() < longPeriod + 1) {
            return signals; // Not enough data
        }
        
        // Apply parameters
        applyParameters();
        
        // Calculate moving averages
        List<BigDecimal> shortMAs = calculateMovingAverage(marketDataList, shortPeriod);
        List<BigDecimal> longMAs = calculateMovingAverage(marketDataList, longPeriod);
        
        // We need at least two points to detect a crossover
        int startIndex = Math.max(shortPeriod, longPeriod);
        if (marketDataList.size() <= startIndex + 1) {
            return signals;
        }
        
        // Check for crossovers
        for (int i = startIndex + 1; i < marketDataList.size(); i++) {
            int maIndex = i - startIndex;
            BigDecimal shortMA = shortMAs.get(maIndex);
            BigDecimal longMA = longMAs.get(maIndex);
            BigDecimal prevShortMA = shortMAs.get(maIndex - 1);
            BigDecimal prevLongMA = longMAs.get(maIndex - 1);
            
            // Check for bullish crossover (short MA crosses above long MA)
            if (prevShortMA.compareTo(prevLongMA) <= 0 && shortMA.compareTo(longMA) > 0) {
                signals.add(createSignal(marketDataList.get(i), SignalType.ENTRY, OrderSide.BUY));
            }
            
            // Check for bearish crossover (short MA crosses below long MA)
            else if (prevShortMA.compareTo(prevLongMA) >= 0 && shortMA.compareTo(longMA) < 0) {
                signals.add(createSignal(marketDataList.get(i), SignalType.ENTRY, OrderSide.SELL));
            }
        }
        
        return signals;
    }
    
    @Override
    public String getType() {
        return "MovingAverageCrossover";
    }
    
    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    private void applyParameters() {
        if (parameters != null) {
            if (parameters.containsKey("shortPeriod")) {
                shortPeriod = ((Number) parameters.get("shortPeriod")).intValue();
            }
            
            if (parameters.containsKey("longPeriod")) {
                longPeriod = ((Number) parameters.get("longPeriod")).intValue();
            }
        }
    }
    
    private List<BigDecimal> calculateMovingAverage(List<MarketData> marketDataList, int period) {
        List<BigDecimal> mas = new ArrayList<>();
        
        for (int i = period; i < marketDataList.size(); i++) {
            BigDecimal sum = BigDecimal.ZERO;
            
            for (int j = i - period + 1; j <= i; j++) {
                sum = sum.add(marketDataList.get(j).getClose());
            }
            
            BigDecimal ma = sum.divide(BigDecimal.valueOf(period), 2, BigDecimal.ROUND_HALF_UP);
            mas.add(ma);
        }
        
        return mas;
    }
    
    private Signal createSignal(MarketData marketData, SignalType type, OrderSide side) {
        return Signal.builder()
                .strategyId(getType())
                .type(type)
                .side(side)
                .symbol(marketData.getSymbol())
                .instrumentToken(marketData.getInstrumentToken())
                .price(marketData.getLastPrice())
                .timestamp(Instant.now())
                .metadata(Map.of(
                        "shortPeriod", shortPeriod,
                        "longPeriod", longPeriod
                ))
                .build();
    }
}
