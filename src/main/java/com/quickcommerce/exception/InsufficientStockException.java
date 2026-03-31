package com.quickcommerce.exception;

/**
 * Thrown when a product does not have sufficient stock to fulfill a request.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
