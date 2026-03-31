package com.quickcommerce.interfaces;

/**
 * Contract for entities that involve a processing step (e.g., Payment, Order).
 * Follows the Interface Segregation Principle (ISP).
 */
public interface Processable {
    /**
     * Executes the core processing logic of the entity.
     *
     * @return true if processing succeeds, false otherwise
     */
    boolean process();

    /**
     * Validates the entity before processing.
     *
     * @return true if the entity is valid for processing
     */
    boolean validate();
}
