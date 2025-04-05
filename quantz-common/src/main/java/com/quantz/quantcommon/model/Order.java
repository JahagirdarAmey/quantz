package com.quantz.quantcommon.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a trading order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    /**
     * Unique identifier for the order
     */
    @Builder.Default
    private String orderId = UUID.randomUUID().toString();
    
    /**
     * Symbol of the instrument
     */
    private String symbol;
    
    /**
     * Broker-specific instrument token
     */
    private String instrumentToken;
    
    /**
     * Side of the order (BUY or SELL)
     */
    private OrderSide side;
    
    /**
     * Type of order (MARKET, LIMIT, etc.)
     */
    private OrderType orderType;
    
    /**
     * Quantity to trade
     */
    private BigDecimal quantity;
    
    /**
     * Limit price (for LIMIT orders)
     */
    private BigDecimal price;
    
    /**
     * Trigger price (for STOP_LOSS orders)
     */
    private BigDecimal triggerPrice;
    
    /**
     * Quantity that has been filled
     */
    private BigDecimal filledQuantity;
    
    /**
     * Average fill price
     */
    private BigDecimal avgFillPrice;
    
    /**
     * Status of the order
     */
    private OrderStatus status;
    
    /**
     * Product type (e.g., DELIVERY, INTRADAY)
     */
    private String productType;
    
    /**
     * Timestamp when the order was created
     */
    @Builder.Default
    private Instant createdAt = Instant.now();
    
    /**
     * Timestamp when the order was last updated
     */
    @Builder.Default
    private Instant updatedAt = Instant.now();
    
    /**
     * Exchange where the order was placed
     */
    private String exchange;
    
    /**
     * Error message if the order was rejected
     */
    private String errorMessage;
    
    /**
     * ID of the signal that generated this order
     */
    private String signalId;
    
    /**
     * ID of the strategy that generated this order
     */
    private String strategyId;
    
    /**
     * Checks if the order is active
     * 
     * @return True if the order is active, false otherwise
     */
    public boolean isActive() {
        return status == OrderStatus.PENDING || 
               status == OrderStatus.OPEN || 
               status == OrderStatus.PARTIALLY_FILLED;
    }
    
    /**
     * Checks if the order is completed
     * 
     * @return True if the order is completed, false otherwise
     */
    public boolean isCompleted() {
        return status == OrderStatus.FILLED || 
               status == OrderStatus.CANCELLED || 
               status == OrderStatus.REJECTED;
    }
    
    /**
     * Calculates the remaining quantity to be filled
     * 
     * @return The remaining quantity
     */
    public BigDecimal getRemainingQuantity() {
        if (quantity == null) {
            return BigDecimal.ZERO;
        }
        
        if (filledQuantity == null) {
            return quantity;
        }
        
        return quantity.subtract(filledQuantity);
    }
}