package com.quantz.quantcommon.model;

import lombok.Getter;

/**
 * Enumeration representing different position types
 */
public enum PositionType {
    /**
     * Intraday position
     */
    INTRADAY("INTRADAY"),
    
    /**
     * Delivery/carry-forward position
     */
    DELIVERY("DELIVERY"),
    
    /**
     * Margin position
     */
    MARGIN("MARGIN");
    
    @Getter
    private final String value;
    
    PositionType(String value) {
        this.value = value;
    }
}
