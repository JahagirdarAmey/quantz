package com.quantz.backtest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for strategy-related operations
 * This is a simplified implementation - in a real system, this would interact with a strategy repository
 */
@Service
public class StrategyService {
    private static final Logger log = LoggerFactory.getLogger(StrategyService.class);
    
    /**
     * Get the name of a strategy by its ID
     * 
     * @param strategyId the ID of the strategy
     * @return the name of the strategy
     */
    public String getStrategyName(UUID strategyId) {
        log.debug("Retrieving name for strategy with ID: {}", strategyId);
        
        // In a real implementation, this would query a strategy repository
        // For now, we return a placeholder name
        
        return "Strategy " + strategyId.toString().substring(0, 8);
    }
}