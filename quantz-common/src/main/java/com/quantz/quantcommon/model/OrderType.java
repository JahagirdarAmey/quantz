package com.quantz.quantcommon.model;

import lombok.Getter;

/**
 * Enumeration representing different order types
 */
public enum OrderType {
    /**
     * Market order - executed at the best available current price
     */
    MARKET("MARKET"),
    
    /**
     * Limit order - executed at the specified price or better
     */
    LIMIT("LIMIT"),
    
    /**
     * Stop loss order - converted to a limit order when the specified price is reached
     */
    STOP_LOSS("SL"),
    
    /**
     * Stop loss market order - converted to a market order when the specified price is reached
     */
    STOP_LOSS_MARKET("SL-M");
    
    @Getter
    private final String value;
    
    OrderType(String value) {
        this.value = value;
    }
}