package com.quantz.quantstrategy.strategy;

import com.quantz.quantcommon.exception.StrategyException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Factory for creating strategy instances
 */
@Component
public class StrategyFactory {
    
    private final Map<String, Function<Map<String, Object>, Strategy>> strategyCreators;
    
    public StrategyFactory() {
        strategyCreators = new HashMap<>();
        
        // Register strategy creators
        registerStrategyCreator("MovingAverageCrossover", MovingAverageCrossoverStrategy::new);
        registerStrategyCreator("BreakoutStrategy", BreakoutStrategy::new);
        registerStrategyCreator("RSIStrategy", RSIStrategy::new);
    }
    
    /**
     * Register a strategy creator function
     * 
     * @param type The strategy type
     * @param creator The creator function
     */
    public void registerStrategyCreator(String type, Function<Map<String, Object>, Strategy> creator) {
        strategyCreators.put(type, creator);
    }
    
    /**
     * Check if a strategy type is supported
     * 
     * @param type The strategy type
     * @return True if supported, false otherwise
     */
    public boolean isSupportedType(String type) {
        return strategyCreators.containsKey(type);
    }
    
    /**
     * Create a strategy instance
     * 
     * @param type The strategy type
     * @param parameters The strategy parameters
     * @return A new strategy instance
     */
    public Strategy createStrategy(String type, Map<String, Object> parameters) {
        Function<Map<String, Object>, Strategy> creator = strategyCreators.get(type);
        
        if (creator == null) {
            throw new StrategyException("Unsupported strategy type: " + type);
        }
        
        Strategy strategy = creator.apply(parameters);
        
        if (parameters != null) {
            strategy.setParameters(parameters);
        }
        
        return strategy;
    }
}