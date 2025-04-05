package com.quantz.quantcommon.exception;

/**
 * Exception thrown when there is an issue with market data
 */
public class MarketDataException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new MarketDataException with the specified message.
     *
     * @param message The detail message
     */
    public MarketDataException(String message) {
        super(message);
    }

    /**
     * Constructs a new MarketDataException with the specified message and cause.
     *
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public MarketDataException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new MarketDataException for when data is missing.
     *
     * @param symbol The symbol for which data is missing
     * @return A new MarketDataException
     */
    public static MarketDataException missingData(String symbol) {
        return new MarketDataException(String.format("Market data missing for symbol: %s", symbol));
    }
    
    /**
     * Constructs a new MarketDataException for when there is an error fetching data.
     *
     * @param symbol The symbol for which there was an error
     * @param cause The cause of the error
     * @return A new MarketDataException
     */
    public static MarketDataException fetchError(String symbol, Throwable cause) {
        return new MarketDataException(
                String.format("Error fetching market data for symbol: %s", symbol), cause);
    }
}