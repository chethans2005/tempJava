package com.quickcommerce.model;

import com.quickcommerce.enums.OrderStatus;
import com.quickcommerce.exception.InvalidOperationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Delivery Partner responsible for picking up and
 * delivering orders to customers.
 *
 * <p>Relationship summary (from UML):
 * <ul>
 *   <li>DeliveryPartner → Order : 1-to-many association (0..*)</li>
 * </ul>
 * </p>
 */
public class DeliveryPartner extends User {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private String             partnerId;
    private boolean            available;
    private final List<Order>  assignedOrders;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a DeliveryPartner.
     *
     * @param name     full name
     * @param email    e-mail address
     * @param password password
     */
    public DeliveryPartner(String name, String email, String password) {
        super(name, email, password);
        this.partnerId      = "DP-" + getUserId().substring(0, 8).toUpperCase();
        this.available      = true;
        this.assignedOrders = new ArrayList<>();
    }

    // -----------------------------------------------------------------------
    // Business methods
    // -----------------------------------------------------------------------

    /**
     * Prints all orders currently assigned to this delivery partner.
     */
    public void viewAssignedOrders() {
        if (assignedOrders.isEmpty()) {
            System.out.println("[DELIVERY] No orders assigned to " + getName());
            return;
        }
        System.out.println("[DELIVERY] Orders assigned to " + getName() + ":");
        assignedOrders.forEach(o ->
                System.out.println("  -> Order " + o.getOrderId() + " | Status: " + o.getStatus()));
    }

    /**
     * Updates the delivery status of an assigned order.
     *
     * @param orderId   the ID of the order to update
     * @param newStatus the new {@link OrderStatus} to apply
     */
    public void updateDeliveryStatus(String orderId, OrderStatus newStatus) {
        assignedOrders.stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst()
                .ifPresentOrElse(order -> {
                    order.setStatus(newStatus);
                    if (newStatus == OrderStatus.DELIVERED) {
                        this.available = true;
                    }
                    System.out.println("[DELIVERY] Order " + orderId + " status updated to: " + newStatus);
                }, () -> {
                    throw new InvalidOperationException(
                            "Order " + orderId + " is not assigned to partner " + getName());
                });
    }

    /**
     * Assigns an order to this delivery partner.
     *
     * @param order the order to assign
     */
    public void assignOrder(Order order) {
        assignedOrders.add(order);
        this.available = false;
        System.out.println("[DELIVERY] Order " + order.getOrderId() + " assigned to " + getName());
    }

    // -----------------------------------------------------------------------
    // Getters & Setters
    // -----------------------------------------------------------------------

    public String        getPartnerId()    { return partnerId; }

    public boolean       isAvailable()     { return available; }
    public void          setAvailable(boolean available) { this.available = available; }

    public List<Order>   getAssignedOrders() {
        return Collections.unmodifiableList(assignedOrders);
    }

    // -----------------------------------------------------------------------
    // User overrides
    // -----------------------------------------------------------------------

    @Override
    public String getRole() {
        return "Delivery Partner";
    }

    @Override
    public String toString() {
        return String.format("DeliveryPartner{partnerId='%s', name='%s', available=%b}",
                partnerId, getName(), available);
    }
}
