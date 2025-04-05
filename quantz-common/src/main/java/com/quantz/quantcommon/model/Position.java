package com.quantz.quantcommon.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a trading position
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    
    /**
     * Unique identifier for the position
     */
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    /**
     * Symbol of the instrument
     */
    private String symbol;
    
    /**
     * Broker-specific instrument token
     */
    private String instrumentToken;
    
    /**
     * Exchange where the position is held
     */
    private String exchange;
    
    /**
     * Quantity of the position
     */
    private BigDecimal quantity;
    
    /**
     * Average price at which the position was taken
     */
    private BigDecimal averagePrice;
    
    /**
     * Last price of the instrument
     */
    private BigDecimal lastPrice;
    
    /**
     * Profit and loss for the position
     */
    private BigDecimal pnl;
    
    /**
     * Product type (e.g., DELIVERY, INTRADAY)
     */
    private String productType;
    
    /**
     * ID of the strategy that created this position
     */
    private String strategyId;
    
    /**
     * Returns true if this is a long position (quantity > 0)
     * 
     * @return True if long, false otherwise
     */
    public boolean isLong() {
        return quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Returns true if this is a short position (quantity < 0)
     * 
     * @return True if short, false otherwise
     */
    public boolean isShort() {
        return quantity != null && quantity.compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * Calculates the current profit and loss
     * 
     * @return The current profit and loss
     */
    public BigDecimal calculatePnl() {
        if (quantity == null || averagePrice == null || lastPrice == null) {
            return BigDecimal.ZERO;
        }
        
        return quantity.multiply(lastPrice.subtract(averagePrice));
    }
    
    /**
     * Calculates the percentage profit and loss
     * 
     * @return The percentage profit and loss
     */
    public BigDecimal calculatePnlPercentage() {
        if (quantity == null || averagePrice == null || lastPrice == null || 
            averagePrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal priceDiff = lastPrice.subtract(averagePrice);
        return priceDiff.divide(averagePrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}