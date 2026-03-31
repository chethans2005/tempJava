package com.quickcommerce.interfaces;

/**
 * Contract for entities that can be tracked (e.g., Orders).
 * Follows the Interface Segregation Principle (ISP).
 */
public interface Trackable {
    /**
     * Returns a human-readable status/tracking summary of the entity.
     *
     * @return tracking information as a String
     */
    String getTrackingInfo();
}
