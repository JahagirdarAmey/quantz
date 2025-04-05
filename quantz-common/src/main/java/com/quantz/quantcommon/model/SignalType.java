package com.quantz.quantcommon.model;

import lombok.Getter;

/**
 * Enumeration representing different types of trading signals
 */
public enum SignalType {
    /**
     * Signal to enter a new position
     */
    ENTRY("ENTRY"),
    
    /**
     * Signal to exit an existing position
     */
    EXIT("EXIT"),
    
    /**
     * Signal to exit an existing position due to stop loss being hit
     */
    STOP_LOSS("STOP_LOSS"),
    
    /**
     * Signal to exit an existing position due to take profit being hit
     */
    TAKE_PROFIT("TAKE_PROFIT"),
    
    /**
     * Signal to adjust position parameters (e.g., move stop loss)
     */
    ADJUSTMENT("ADJUSTMENT");
    
    @Getter
    private final String value;
    
    SignalType(String value) {
        this.value = value;
    }
}