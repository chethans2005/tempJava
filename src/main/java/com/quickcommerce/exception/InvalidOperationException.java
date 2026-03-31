package com.quickcommerce.exception;

/**
 * Thrown when an operation is invalid in the current system state
 * (e.g., cancelling an already-delivered order).
 */
public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(String message) {
        super(message);
    }
}
