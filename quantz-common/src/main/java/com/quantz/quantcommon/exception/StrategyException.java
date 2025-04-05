package com.quantz.quantcommon.exception;

public class StrategyException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new StrategyException with the specified message.
     *
     * @param message The detail message
     */
    public StrategyException(String message) {
        super(message);
    }

    /**
     * Constructs a new StrategyException with the specified message and cause.
     *
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public StrategyException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new StrategyException with a formatted message for strategy validation errors.
     *
     * @param strategyId The ID of the strategy
     * @param field The field that failed validation
     * @param value The invalid value
     * @return A new StrategyException
     */
    public static StrategyException invalidField(String strategyId, String field, Object value) {
        return new StrategyException(
                String.format("Invalid value for %s: '%s' in strategy %s", field, value, strategyId));
    }
    
    /**
     * Constructs a new StrategyException for when a strategy is not found.
     *
     * @param strategyId The ID of the strategy that wasn't found
     * @return A new StrategyException
     */
    public static StrategyException notFound(String strategyId) {
        return new StrategyException(String.format("Strategy not found with ID: %s", strategyId));
    }
    
    /**
     * Constructs a new StrategyException for when a strategy type is not supported.
     *
     * @param type The unsupported strategy type
     * @return A new StrategyException
     */
    public static StrategyException unsupportedType(String type) {
        return new StrategyException(String.format("Unsupported strategy type: %s", type));
    }
}