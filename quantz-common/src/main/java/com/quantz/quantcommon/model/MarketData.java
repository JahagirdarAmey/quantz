package com.quantz.quantcommon.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents market price data for a security
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketData {
    
    /**
     * Symbol of the instrument
     */
    private String symbol;
    
    /**
     * Broker-specific instrument token
     */
    private String instrumentToken;
    
    /**
     * Last traded price
     */
    private BigDecimal lastPrice;
    
    /**
     * Opening price of the period
     */
    private BigDecimal open;
    
    /**
     * Highest price of the period
     */
    private BigDecimal high;
    
    /**
     * Lowest price of the period
     */
    private BigDecimal low;
    
    /**
     * Closing price of the period
     */
    private BigDecimal close;
    
    /**
     * Trading volume for the period
     */
    private Long volume;
    
    /**
     * Timestamp of the data point
     */
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    /**
     * Exchange where the instrument is traded
     */
    private String exchange;
    
    /**
     * Timeframe of the data (e.g., "1m", "5m", "15m", "1h", "1d")
     */
    private String timeframe;
    
    /**
     * Additional metadata
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * Returns the typical price (high + low + close) / 3
     * 
     * @return The typical price
     */
    public BigDecimal getTypicalPrice() {
        if (high == null || low == null || close == null) {
            return null;
        }
        
        return high.add(low).add(close)
                .divide(BigDecimal.valueOf(3), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Returns the price range (high - low)
     * 
     * @return The price range
     */
    public BigDecimal getRange() {
        if (high == null || low == null) {
            return null;
        }
        
        return high.subtract(low);
    }
    
    /**
     * Returns the body size (absolute difference between open and close)
     * 
     * @return The body size
     */
    public BigDecimal getBodySize() {
        if (open == null || close == null) {
            return null;
        }
        
        return open.subtract(close).abs();
    }
    
    /**
     * Returns true if this is a bullish candle (close > open)
     * 
     * @return True if bullish, false otherwise
     */
    public boolean isBullish() {
        if (open == null || close == null) {
            return false;
        }
        
        return close.compareTo(open) > 0;
    }
    
    /**
     * Returns true if this is a bearish candle (close < open)
     * 
     * @return True if bearish, false otherwise
     */
    public boolean isBearish() {
        if (open == null || close == null) {
            return false;
        }
        
        return close.compareTo(open) < 0;
    }
    
    /**
     * Returns true if this is a doji candle (open ≈ close)
     * 
     * @return True if doji, false otherwise
     */
    public boolean isDoji() {
        if (open == null || close == null || high == null || low == null) {
            return false;
        }
        
        BigDecimal range = getRange();
        if (range.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        
        // Doji if body size is less than 10% of the range
        BigDecimal bodyToRangeRatio = getBodySize().divide(range, 2, BigDecimal.ROUND_HALF_UP);
        return bodyToRangeRatio.compareTo(BigDecimal.valueOf(0.1)) < 0;
    }
}