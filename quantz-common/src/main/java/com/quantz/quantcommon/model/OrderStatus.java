package com.quantz.quantcommon.model;

import lombok.Getter;

/**
 * Enumeration representing different order statuses
 */
public enum OrderStatus {
    /**
     * Order has been created but not placed with the broker yet
     */
    PENDING("PENDING"),
    
    /**
     * Order has been placed with the broker and is open
     */
    OPEN("OPEN"),
    
    /**
     * Order has been completely filled
     */
    FILLED("FILLED"),
    
    /**
     * Order has been partially filled
     */
    PARTIALLY_FILLED("PARTIALLY_FILLED"),
    
    /**
     * Order has been cancelled
     */
    CANCELLED("CANCELLED"),
    
    /**
     * Order has been rejected by the broker
     */
    REJECTED("REJECTED"),
    
    /**
     * Order status is unknown
     */
    UNKNOWN("UNKNOWN");
    
    @Getter
    private final String value;
    
    OrderStatus(String value) {
        this.value = value;
    }
}