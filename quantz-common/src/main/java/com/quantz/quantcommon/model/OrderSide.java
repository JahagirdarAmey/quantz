package com.quantz.quantcommon.model;

import lombok.Getter;

/**
 * Enumeration representing the side of an order (buy or sell)
 */
public enum OrderSide {
    /**
     * Buy order
     */
    BUY("BUY"),
    
    /**
     * Sell order
     */
    SELL("SELL");
    
    @Getter
    private final String value;
    
    OrderSide(String value) {
        this.value = value;
    }
}