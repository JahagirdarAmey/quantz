package com.quantz.quantcommon.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a trading signal generated by a strategy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Signal {
    
    /**
     * Unique identifier for the signal
     */
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    /**
     * ID of the strategy that generated this signal
     */
    private String strategyId;
    
    /**
     * Symbol of the instrument
     */
    private String symbol;
    
    /**
     * Broker-specific instrument token
     */
    private String instrumentToken;
    
    /**
     * Type of signal (ENTRY, EXIT, etc.)
     */
    private SignalType type;
    
    /**
     * Side of the order (BUY or SELL)
     */
    private OrderSide side;
    
    /**
     * Price at which the signal was generated
     */
    private BigDecimal price;
    
    /**
     * Quantity to trade
     */
    private BigDecimal quantity;
    
    /**
     * Stop loss price
     */
    private BigDecimal stopLoss;
    
    /**
     * Take profit price
     */
    private BigDecimal takeProfit;
    
    /**
     * Timestamp when the signal was generated
     */
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    /**
     * Additional metadata for the signal (strategy-specific)
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * Whether the signal has been processed
     */
    @Builder.Default
    private boolean processed = false;
    
    /**
     * Result of the signal (profit/loss)
     */
    private BigDecimal result;
    
    /**
     * ID of the order created from this signal
     */
    private String orderId;
    
    /**
     * Calculates the risk-to-reward ratio of this signal
     * 
     * @return The risk-to-reward ratio, or null if stop loss or take profit is not set
     */
    public BigDecimal getRiskRewardRatio() {
        if (stopLoss == null || takeProfit == null || price == null) {
            return null;
        }
        
        BigDecimal risk;
        BigDecimal reward;
        
        if (side == OrderSide.BUY) {
            risk = price.subtract(stopLoss);
            reward = takeProfit.subtract(price);
        } else {
            risk = stopLoss.subtract(price);
            reward = price.subtract(takeProfit);
        }
        
        if (risk.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        
        return reward.divide(risk, 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Checks if this signal is valid
     * 
     * @return True if the signal is valid, false otherwise
     */
    public boolean isValid() {
        return strategyId != null && !strategyId.isEmpty() &&
               symbol != null && !symbol.isEmpty() &&
               type != null &&
               side != null &&
               price != null &&
               price.compareTo(BigDecimal.ZERO) > 0;
    }
}