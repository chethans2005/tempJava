package com.quickcommerce.exception;

/**
 * Thrown when a requested resource (User, Product, Order, etc.) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
