package com.quickcommerce.model;

import com.quickcommerce.exception.InvalidOperationException;

/**
 * Represents a single line-item inside a {@link Cart}.
 *
 * <p>Relationship summary (from UML):
 * <ul>
 *   <li>CartItem → Product : many-to-1 association (references the product)</li>
 *   <li>Cart     → CartItem : 1-to-many composition (owns the items)</li>
 * </ul>
 * </p>
 *
 * <p>The price is captured at the time the item is added so that a price
 * change on the Product does not silently alter cart totals (snapshot pricing).</p>
 */
public class CartItem {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final Product product;
    private int           quantity;
    private final double  priceAtAddition;   // Snapshot price — immutable after creation

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a CartItem for the given product and quantity.
     *
     * @param product  the product being added
     * @param quantity the number of units (must be >= 1)
     */
    public CartItem(Product product, int quantity) {
        if (quantity <= 0)
            throw new InvalidOperationException("Cart item quantity must be at least 1.");
        if (!product.isInStock())
            throw new InvalidOperationException("Product '" + product.getName() + "' is out of stock.");

        this.product         = product;
        this.quantity        = quantity;
        this.priceAtAddition = product.getPrice();
    }

    // -----------------------------------------------------------------------
    // Business methods
    // -----------------------------------------------------------------------

    /**
     * Calculates the total cost for this line-item.
     *
     * @return quantity × price at time of addition
     */
    public double getTotal() {
        return quantity * priceAtAddition;
    }

    // -----------------------------------------------------------------------
    // Getters & Setters
    // -----------------------------------------------------------------------

    public Product getProduct()            { return product; }

    public int     getQuantity()           { return quantity; }
    public void    setQuantity(int quantity) {
        if (quantity <= 0)
            throw new InvalidOperationException("Quantity must be at least 1.");
        this.quantity = quantity;
    }

    public double  getPriceAtAddition()    { return priceAtAddition; }

    // -----------------------------------------------------------------------
    // Object overrides
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("CartItem{product='%s', qty=%d, price=INR %.2f, total=INR %.2f}",
                product.getName(), quantity, priceAtAddition, getTotal());
    }
}
