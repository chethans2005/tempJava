package com.quickcommerce.model;

import com.quickcommerce.enums.OrderStatus;
import com.quickcommerce.enums.PaymentMethod;
import com.quickcommerce.exception.InvalidOperationException;
import com.quickcommerce.interfaces.Trackable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents a customer order in the Quick Commerce system.
 *
 * <p>Implements {@link Trackable} so its state can be queried
 * uniformly via a common interface.</p>
 *
 * <p>Relationship summary (from UML):
 * <ul>
 *   <li>Order  → OrderItem : 1-to-many composition (1..*)  — black diamond on Order</li>
 *   <li>Order  → Payment   : 1-to-1  composition           — black diamond on Order</li>
 *   <li>Customer → Order   : 1-to-many association (1..*)</li>
 *   <li>DeliveryPartner → Order : 0..* association</li>
 * </ul>
 * </p>
 */
public class Order implements Trackable {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final String          orderId;
    private final LocalDateTime   orderDate;
    private       OrderStatus     status;
    private       double          totalAmount;

    private final Customer        customer;
    private final List<OrderItem> orderItems;
    private       Payment         payment;           // Composition — created during checkout
    private       DeliveryPartner assignedPartner;   // Set by the delivery service

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new Order in PENDING state for the given customer.
     *
     * @param customer the customer placing the order
     */
    public Order(Customer customer) {
        this.orderId    = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.orderDate  = LocalDateTime.now();
        this.status     = OrderStatus.PENDING;
        this.customer   = customer;
        this.orderItems = new ArrayList<>();
        this.totalAmount = 0.0;
    }

    // -----------------------------------------------------------------------
    // Business methods
    // -----------------------------------------------------------------------

    /**
     * Populates the order with items from the customer's cart,
     * deducts stock from each product, and calculates the total.
     *
     * @param cart the customer's current cart (must not be empty)
     */
    public void createOrder(Cart cart) {
        if (cart.isEmpty()) {
            throw new InvalidOperationException("Cannot create an order from an empty cart.");
        }

        for (CartItem cartItem : cart.getCartItems()) {
            // Deduct stock from the product
            cartItem.getProduct().decreaseStock(cartItem.getQuantity());
            // Convert cart item to an immutable order item
            orderItems.add(OrderItem.fromCartItem(cartItem));
        }

        this.totalAmount = orderItems.stream()
                .mapToDouble(OrderItem::getLineTotal)
                .sum();

        System.out.println("[ORDER] Order " + orderId + " created. Total: INR " + totalAmount);
    }

    /**
     * Cancels the order if it has not yet been delivered.
     * Restores stock for every cancelled item and refunds payment if it exists.
     */
    public void cancelOrder() {
        if (status == OrderStatus.DELIVERED) {
            throw new InvalidOperationException("Cannot cancel a delivered order.");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new InvalidOperationException("Order is already cancelled.");
        }

        // Restore stock
        orderItems.forEach(item ->
                item.getProduct().increaseStock(item.getQuantity()));

        // Refund if payment was made
        if (payment != null && payment.getPaymentStatus().name().equals("SUCCESS")) {
            payment.refund();
        }

        this.status = OrderStatus.CANCELLED;
        System.out.println("[ORDER] Order " + orderId + " has been cancelled.");
    }

    /**
     * Attaches a {@link Payment} to this order and processes it.
     *
     * @param paymentMethod the customer's chosen payment method
     * @return {@code true} if payment was successful
     */
    public boolean checkout(PaymentMethod paymentMethod) {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOperationException("Only PENDING orders can be checked out.");
        }
        this.payment = new Payment(totalAmount, paymentMethod);
        boolean success = payment.processPayment();
        if (success) {
            this.status = OrderStatus.CONFIRMED;
        }
        return success;
    }

    // -----------------------------------------------------------------------
    // Trackable implementation
    // -----------------------------------------------------------------------

    /**
     * Returns a formatted, human-readable tracking summary.
     */
    @Override
    public String getTrackingInfo() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String partnerInfo = (assignedPartner != null)
                ? assignedPartner.getName() + " (" + assignedPartner.getPartnerId() + ")"
                : "Not yet assigned";

        return String.format(
                "Order ID    : %s%n" +
                "Date        : %s%n" +
                "Status      : %s%n" +
                "Delivery By : %s%n" +
                "Total       : INR %.2f",
                orderId, orderDate.format(fmt),
                status.getDisplayName(), partnerInfo, totalAmount);
    }

    // -----------------------------------------------------------------------
    // Getters & Setters
    // -----------------------------------------------------------------------

    public String          getOrderId()           { return orderId; }
    public LocalDateTime   getOrderDate()          { return orderDate; }
    public OrderStatus     getStatus()             { return status; }
    public void            setStatus(OrderStatus s){ this.status = s; }
    public double          getTotalAmount()        { return totalAmount; }
    public Customer        getCustomer()           { return customer; }
    public Payment         getPayment()            { return payment; }
    public DeliveryPartner getAssignedPartner()    { return assignedPartner; }
    public void            setAssignedPartner(DeliveryPartner dp) { this.assignedPartner = dp; }

    /** Returns an unmodifiable view of the order items. */
    public List<OrderItem>  getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    // -----------------------------------------------------------------------
    // Object overrides
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("Order{id='%s', customer='%s', status=%s, total=INR %.2f}",
                orderId, customer.getName(), status, totalAmount);
    }
}
