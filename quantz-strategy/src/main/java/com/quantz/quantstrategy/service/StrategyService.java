package com.quantz.quantstrategy.service;


import com.quantz.quantstrategy.model.Strategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service interface for strategy operations
 */
public interface StrategyService {

    /**
     * Create a new strategy
     * 
     * @param strategy The strategy to create
     * @return A Mono containing the created strategy
     */
    Mono<Strategy> createStrategy(Strategy strategy);
    
    /**
     * Get a strategy by ID
     * 
     * @param id The strategy ID
     * @return A Mono containing the strategy
     */
    Mono<Strategy> getStrategyById(String id);
    
    /**
     * Get all strategies
     * 
     * @return A Flux of strategies
     */
    Flux<Strategy> getAllStrategies();
    
    /**
     * Update a strategy
     * 
     * @param id The strategy ID
     * @param strategy The updated strategy
     * @return A Mono containing the updated strategy
     */
    Mono<Strategy> updateStrategy(String id, Strategy strategy);
    
    /**
     * Delete a strategy
     * 
     * @param id The strategy ID
     * @return A Mono of Void
     */
    Mono<Void> deleteStrategy(String id);
    
    /**
     * Generate signals for a strategy based on market data
     * 
     * @param strategyId The strategy ID
     * @param marketDataList The market data to process
     * @return A Flux of signals
     */
    Flux<Signal> generateSignals(String strategyId, List<MarketData> marketDataList);
    
    /**
     * Start a strategy
     * 
     * @param id The strategy ID
     * @return A Mono containing the updated strategy
     */
    Mono<Strategy> startStrategy(String id);
    
    /**
     * Stop a strategy
     * 
     * @param id The strategy ID
     * @return A Mono containing the updated strategy
     */
    Mono<Strategy> stopStrategy(String id);
}
