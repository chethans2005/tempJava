package com.quickcommerce.model;

import com.quickcommerce.exception.InvalidOperationException;

/**
 * Represents a single line-item inside an {@link Order}.
 *
 * <p>Unlike {@link CartItem}, an OrderItem is immutable after creation —
 * its quantity and price are locked in at the moment the order is placed
 * to preserve a faithful record of what was purchased.</p>
 *
 * <p>Relationship summary (from UML):
 * <ul>
 *   <li>Order     → OrderItem : 1-to-many composition (1..*)</li>
 *   <li>OrderItem → Product   : many-to-1 association  (*)</li>
 * </ul>
 * </p>
 */
public class OrderItem {

    // -----------------------------------------------------------------------
    // Fields — all final (immutable record)
    // -----------------------------------------------------------------------

    private final Product product;
    private final int     quantity;
    private final double  price;      // Unit price at time of order placement

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates an immutable order line-item.
     *
     * @param product  the product ordered
     * @param quantity number of units (must be >= 1)
     * @param price    unit price at time of order (must be >= 0)
     */
    public OrderItem(Product product, int quantity, double price) {
        if (quantity <= 0)
            throw new InvalidOperationException("Order item quantity must be at least 1.");
        if (price < 0)
            throw new InvalidOperationException("Order item price cannot be negative.");

        this.product  = product;
        this.quantity = quantity;
        this.price    = price;
    }

    // -----------------------------------------------------------------------
    // Convenience factory — create from a CartItem
    // -----------------------------------------------------------------------

    /**
     * Factory method that converts a {@link CartItem} into an {@link OrderItem}.
     *
     * @param cartItem the source cart item
     * @return a new OrderItem with the same product, quantity, and snapshot price
     */
    public static OrderItem fromCartItem(CartItem cartItem) {
        return new OrderItem(
                cartItem.getProduct(),
                cartItem.getQuantity(),
                cartItem.getPriceAtAddition()
        );
    }

    // -----------------------------------------------------------------------
    // Business methods
    // -----------------------------------------------------------------------

    /**
     * Returns the line total (unit price × quantity).
     *
     * @return total cost for this order item
     */
    public double getLineTotal() {
        return quantity * price;
    }

    // -----------------------------------------------------------------------
    // Getters (no setters — immutable)
    // -----------------------------------------------------------------------

    public Product getProduct()  { return product; }
    public int     getQuantity() { return quantity; }
    public double  getPrice()    { return price; }

    // -----------------------------------------------------------------------
    // Object overrides
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("OrderItem{product='%s', qty=%d, unitPrice=INR %.2f, lineTotal=INR %.2f}",
                product.getName(), quantity, price, getLineTotal());
    }
}
