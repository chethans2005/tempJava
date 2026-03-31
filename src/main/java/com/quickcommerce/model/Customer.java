package com.quickcommerce.model;

import com.quickcommerce.exception.InvalidOperationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Customer — the primary buyer in the Quick Commerce system.
 *
 * <p>Inherits authentication behaviour from {@link User} and adds
 * shopping-specific responsibilities: managing a {@link Cart} and
 * placing / tracking {@link Order}s.</p>
 *
 * <p>Relationship summary (from UML):
 * <ul>
 *   <li>Customer → Cart  : 1-to-1 composition (owns one cart)</li>
 *   <li>Customer → Order : 1-to-many association (places many orders)</li>
 * </ul>
 * </p>
 */
public class Customer extends User {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private String deliveryAddress;
    private Cart   cart;                             // Composition: 1-to-1
    private final List<Order> orderHistory;          // Association: 1-to-many

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a Customer with a fresh, empty {@link Cart}.
     *
     * @param name            full name
     * @param email           e-mail address
     * @param password        password
     * @param deliveryAddress default delivery address
     */
    public Customer(String name, String email, String password, String deliveryAddress) {
        super(name, email, password);
        this.deliveryAddress = deliveryAddress;
        this.cart            = new Cart(this);       // Each customer owns exactly one cart
        this.orderHistory    = new ArrayList<>();
    }

    // -----------------------------------------------------------------------
    // Business methods
    // -----------------------------------------------------------------------

    /**
     * Adds a product to the customer's cart.
     *
     * @param product  the product to add
     * @param quantity how many units
     */
    public void addToCart(Product product, int quantity) {
        if (!isLoggedIn()) {
            throw new InvalidOperationException("Customer must be logged in to add items to cart.");
        }
        cart.addItem(product, quantity);
        System.out.println("[CART] Added " + quantity + "x '" + product.getName() + "' to cart.");
    }

    /**
     * Converts the current cart into a new {@link Order}.
     *
     * @return the newly created Order
     */
    public Order placeOrder() {
        if (!isLoggedIn()) {
            throw new InvalidOperationException("Customer must be logged in to place an order.");
        }
        if (cart.getCartItems().isEmpty()) {
            throw new InvalidOperationException("Cannot place an order with an empty cart.");
        }

        Order order = new Order(this);
        order.createOrder(cart);
        orderHistory.add(order);

        // Clear cart after placing order
        cart = new Cart(this);
        System.out.println("[ORDER] Order placed successfully. Order ID: " + order.getOrderId());
        return order;
    }

    /**
     * Prints the tracking status of a specific order.
     *
     * @param orderId the ID of the order to track
     */
    public void trackOrder(String orderId) {
        orderHistory.stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst()
                .ifPresentOrElse(
                        o -> System.out.println("[TRACK] " + o.getTrackingInfo()),
                        () -> System.out.println("[TRACK] Order not found: " + orderId)
                );
    }

    // -----------------------------------------------------------------------
    // Getters & Setters
    // -----------------------------------------------------------------------

    public String getDeliveryAddress()                      { return deliveryAddress; }
    public void   setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public Cart   getCart()                                 { return cart; }

    /** Returns an unmodifiable view of the order history. */
    public List<Order> getOrderHistory() {
        return Collections.unmodifiableList(orderHistory);
    }

    // -----------------------------------------------------------------------
    // User overrides
    // -----------------------------------------------------------------------

    @Override
    public String getRole() {
        return "Customer";
    }

    @Override
    public String toString() {
        return String.format("Customer{id='%s', name='%s', email='%s', address='%s'}",
                getUserId(), getName(), getEmail(), deliveryAddress);
    }
}
