package com.quantz.quantstrategy.strategy;

import com.yourdomain.quantcommon.model.MarketData;
import com.yourdomain.quantcommon.model.OrderSide;
import com.yourdomain.quantcommon.model.Signal;
import com.yourdomain.quantcommon.model.SignalType;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of an RSI (Relative Strength Index) strategy.
 * 
 * Generates BUY signals when RSI falls below oversold level,
 * and SELL signals when RSI rises above overbought level.
 */
@Slf4j
@NoArgsConstructor
public class RSIStrategy implements Strategy {

    private Map<String, Object> parameters = new HashMap<>();
    
    // Default parameter values
    private int rsiPeriod = 14;
    private int oversoldLevel = 30;
    private int overboughtLevel = 70;
    
    public RSIStrategy(Map<String, Object> parameters) {
        setParameters(parameters);
    }
    
    @Override
    public List<Signal> generateSignals(List<MarketData> marketDataList) {
        List<Signal> signals = new ArrayList<>();
        
        if (marketDataList == null || marketDataList.size() < rsiPeriod + 1) {
            return signals; // Not enough data
        }
        
        // Apply parameters
        applyParameters();
        
        // Calculate RSI
        List<BigDecimal> rsiValues = calculateRSI(marketDataList);
        
        // Check for RSI signals
        for (int i = 1; i < rsiValues.size(); i++) {
            BigDecimal currentRSI = rsiValues.get(i);
            BigDecimal previousRSI = rsiValues.get(i - 1);
            int dataIndex = i + rsiPeriod;
            
            // Oversold condition (RSI crosses above oversold level)
            if (previousRSI.compareTo(BigDecimal.valueOf(oversoldLevel)) <= 0 && 
                currentRSI.compareTo(BigDecimal.valueOf(oversoldLevel)) > 0) {
                
                signals.add(createSignal(marketDataList.get(dataIndex), SignalType.ENTRY, OrderSide.BUY, currentRSI));
            }
            
            // Overbought condition (RSI crosses below overbought level)
            else if (previousRSI.compareTo(BigDecimal.valueOf(overboughtLevel)) >= 0 && 
                    currentRSI.compareTo(BigDecimal.valueOf(overboughtLevel)) < 0) {
                
                signals.add(createSignal(marketDataList.get(dataIndex), SignalType.ENTRY, OrderSide.SELL, currentRSI));
            }
        }
        
        return signals;
    }
    
    @Override
    public String getType() {
        return "RSIStrategy";
    }
    
    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    private void applyParameters() {
        if (parameters != null) {
            if (parameters.containsKey("rsiPeriod")) {
                rsiPeriod = ((Number) parameters.get("rsiPeriod")).intValue();
            }
            
            if (parameters.containsKey("oversoldLevel")) {
                oversoldLevel = ((Number) parameters.get("oversoldLevel")).intValue();
            }
            
            if (parameters.containsKey("overboughtLevel")) {
                overboughtLevel = ((Number) parameters.get("overboughtLevel")).intValue();
            }
        }
    }
    
    private List<BigDecimal> calculateRSI(List<MarketData> marketDataList) {
        List<BigDecimal> rsiValues = new ArrayList<>();
        
        // Calculate price changes
        List<BigDecimal> priceChanges = new ArrayList<>();
        for (int i = 1; i < marketDataList.size(); i++) {
            BigDecimal currentClose = marketDataList.get(i).getClose();
            BigDecimal previousClose = marketDataList.get(i - 1).getClose();
            priceChanges.add(currentClose.subtract(previousClose));
        }
        
        // Calculate initial average gain and loss
        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;
        
        for (int i = 0; i < rsiPeriod; i++) {
            BigDecimal change = priceChanges.get(i);
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                avgGain = avgGain.add(change);
            } else {
                avgLoss = avgLoss.add(change.abs());
            }
        }
        
        avgGain = avgGain.divide(BigDecimal.valueOf(rsiPeriod), 2, RoundingMode.HALF_UP);
        avgLoss = avgLoss.divide(BigDecimal.valueOf(rsiPeriod), 2, RoundingMode.HALF_UP);
        
        // Calculate RSI
        for (int i = rsiPeriod; i < priceChanges.size(); i++) {
            BigDecimal rs;
            
            if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
                rs = BigDecimal.valueOf(100);
            } else {
                rs = avgGain.divide(avgLoss, 2, RoundingMode.HALF_UP);
            }
            
            BigDecimal rsi = BigDecimal.valueOf(100).subtract(
                    BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 2, RoundingMode.HALF_UP));
            
            rsiValues.add(rsi);
            
            // Update average gain and loss for the next period
            BigDecimal currentChange = priceChanges.get(i);
            BigDecimal currentGain = (currentChange.compareTo(BigDecimal.ZERO) > 0) ? currentChange : BigDecimal.ZERO;
            BigDecimal currentLoss = (currentChange.compareTo(BigDecimal.ZERO) < 0) ? currentChange.abs() : BigDecimal.ZERO;
            
            avgGain = (avgGain.multiply(BigDecimal.valueOf(rsiPeriod - 1)).add(currentGain))
                    .divide(BigDecimal.valueOf(rsiPeriod), 2, RoundingMode.HALF_UP);
            
            avgLoss = (avgLoss.multiply(BigDecimal.valueOf(rsiPeriod - 1)).add(currentLoss))
                    .divide(BigDecimal.valueOf(rsiPeriod), 2, RoundingMode.HALF_UP);
        }
        
        return rsiValues;
    }
    
    private Signal createSignal(MarketData marketData, SignalType type, OrderSide side, BigDecimal rsiValue) {
        BigDecimal stopLoss;
        BigDecimal takeProfit;
        
        if (side == OrderSide.BUY) {
            // For buy signals, set stop loss 2% below entry price
            stopLoss = marketData.getClose().multiply(BigDecimal.valueOf(0.98));
            // And take profit 4% above entry price (1:2 risk-reward ratio)
            takeProfit = marketData.getClose().multiply(BigDecimal.valueOf(1.04));
        } else {
            // For sell signals, set stop loss 2% above entry price
            stopLoss = marketData.getClose().multiply(BigDecimal.valueOf(1.02));
            // And take profit 4% below entry price (1:2 risk-reward ratio)
            takeProfit = marketData.getClose().multiply(BigDecimal.valueOf(0.96));
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
                        "rsiPeriod", rsiPeriod,
                        "rsiValue", rsiValue,
                        "oversoldLevel", oversoldLevel,
                        "overboughtLevel", overboughtLevel
                ))
                .build();
    }
}