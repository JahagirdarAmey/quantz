package com.quantz.quantcommon.exception;

/**
 * Exception thrown when there is an issue with order execution
 */
public class OrderExecutionException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new OrderExecutionException with the specified message.
     *
     * @param message The detail message
     */
    public OrderExecutionException(String message) {
        super(message);
    }

    /**
     * Constructs a new OrderExecutionException with the specified message and cause.
     *
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public OrderExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new OrderExecutionException for when an order is rejected.
     *
     * @param orderId The ID of the rejected order
     * @param reason The reason for rejection
     * @return A new OrderExecutionException
     */
    public static OrderExecutionException orderRejected(String orderId, String reason) {
        return new OrderExecutionException(
                String.format("Order %s rejected: %s", orderId, reason));
    }
    
    /**
     * Constructs a new OrderExecutionException for when there is an error placing an order.
     *
     * @param symbol The symbol for which there was an error
     * @param cause The cause of the error
     * @return A new OrderExecutionException
     */
    public static OrderExecutionException placementError(String symbol, Throwable cause) {
        return new OrderExecutionException(
                String.format("Error placing order for symbol: %s", symbol), cause);
    }
}