package com.quantz.quantcommon.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a portfolio of positions and orders
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Unique identifier for the portfolio
     */
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    /**
     * Name of the portfolio
     */
    private String name;
    
    /**
     * Initial capital
     */
    private BigDecimal initialCapital;
    
    /**
     * Current cash balance
     */
    private BigDecimal cash;
    
    /**
     * List of positions
     */
    @Builder.Default
    private List<Position> positions = new ArrayList<>();
    
    /**
     * List of orders
     */
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
    
    /**
     * ID of the strategy that created this portfolio
     */
    private String strategyId;
    
    /**
     * Creation timestamp
     */
    @Builder.Default
    private Instant createdAt = Instant.now();
    
    /**
     * Last update timestamp
     */
    @Builder.Default
    private Instant updatedAt = Instant.now();
    
    /**
     * Calculates the total portfolio value
     * 
     * @return The total portfolio value
     */
    public BigDecimal getTotalValue() {
        BigDecimal positionsValue = positions.stream()
                .map(p -> p.getQuantity().multiply(p.getLastPrice() != null ? p.getLastPrice() : p.getAveragePrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return cash != null ? cash.add(positionsValue) : positionsValue;
    }
    
    /**
     * Calculates the total profit and loss
     * 
     * @return The total profit and loss
     */
    public BigDecimal getTotalPnl() {
        if (initialCapital == null) {
            return BigDecimal.ZERO;
        }
        
        return getTotalValue().subtract(initialCapital);
    }
    
    /**
     * Calculates the total profit and loss as a percentage
     * 
     * @return The total profit and loss as a percentage
     */
    public BigDecimal getTotalPnlPercentage() {
        if (initialCapital == null || initialCapital.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return getTotalPnl()
                .divide(initialCapital, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * Adds a position to the portfolio
     * 
     * @param position The position to add
     */
    public void addPosition(Position position) {
        if (position == null) {
            return;
        }
        
        // Check if position already exists for this instrument
        for (int i = 0; i < positions.size(); i++) {
            Position existingPosition = positions.get(i);
            
            if (existingPosition.getInstrumentToken().equals(position.getInstrumentToken())) {
                // Update existing position
                BigDecimal totalQuantity = existingPosition.getQuantity().add(position.getQuantity());
                
                // If total quantity is zero, remove the position
                if (totalQuantity.compareTo(BigDecimal.ZERO) == 0) {
                    positions.remove(i);
                    return;
                }
                
                // Calculate new average price
                BigDecimal existingValue = existingPosition.getQuantity().multiply(existingPosition.getAveragePrice());
                BigDecimal newValue = position.getQuantity().multiply(position.getAveragePrice());
                BigDecimal totalValue = existingValue.add(newValue);
                BigDecimal newAveragePrice = totalValue.divide(totalQuantity, 2, BigDecimal.ROUND_HALF_UP);
                
                // Update position
                existingPosition.setQuantity(totalQuantity);
                existingPosition.setAveragePrice(newAveragePrice);
                existingPosition.setLastPrice(position.getLastPrice());
                existingPosition.setPnl(existingPosition.calculatePnl());
                
                return;
            }
        }
        
        // Add new position
        positions.add(position);
    }
    
    /**
     * Adds an order to the portfolio
     * 
     * @param order The order to add
     */
    public void addOrder(Order order) {
        if (order == null) {
            return;
        }
        
        orders.add(order);
    }
}
