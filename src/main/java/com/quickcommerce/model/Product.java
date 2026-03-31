package com.quickcommerce.model;

import com.quickcommerce.exception.InsufficientStockException;
import com.quickcommerce.exception.InvalidOperationException;
import java.util.UUID;

/**
 * Represents a Product listed on the Quick Commerce platform.
 *
 * <p>Relationship summary (from UML):
 * <ul>
 *   <li>CartItem  → Product : many-to-1 (a cart item references one product)</li>
 *   <li>OrderItem → Product : many-to-1 (an order item references one product)</li>
 * </ul>
 * </p>
 */
public class Product {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final String productId;
    private String       name;
    private String       category;
    private double       price;
    private int          stockQuantity;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new Product with an auto-generated ID.
     *
     * @param name          product name
     * @param category      product category (e.g., "Dairy", "Bakery")
     * @param price         unit price (must be non-negative)
     * @param stockQuantity initial stock (must be non-negative)
     */
    public Product(String name, String category, double price, int stockQuantity) {
        if (price < 0)         throw new InvalidOperationException("Price cannot be negative.");
        if (stockQuantity < 0) throw new InvalidOperationException("Stock quantity cannot be negative.");

        this.productId     = "PRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.name          = name;
        this.category      = category;
        this.price         = price;
        this.stockQuantity = stockQuantity;
    }

    /**
     * Reconstruct a Product with an explicit productId (used when loading from persistence).
     */
    public Product(String productId, String name, String category, double price, int stockQuantity) {
        if (price < 0)         throw new InvalidOperationException("Price cannot be negative.");
        if (stockQuantity < 0) throw new InvalidOperationException("Stock quantity cannot be negative.");
        this.productId     = productId;
        this.name          = name;
        this.category      = category;
        this.price         = price;
        this.stockQuantity = stockQuantity;
    }

    // -----------------------------------------------------------------------
    // Business methods
    // -----------------------------------------------------------------------

    /**
     * Sets the stock to a new absolute value.
     *
     * @param newQuantity new stock level (must be >= 0)
     */
    public void updateStock(int newQuantity) {
        if (newQuantity < 0) throw new InvalidOperationException("Stock cannot be set to a negative value.");
        this.stockQuantity = newQuantity;
        System.out.println("[PRODUCT] Stock for '" + name + "' updated to " + newQuantity);
    }

    /**
     * Reduces the stock by the requested quantity (used when an order is placed).
     *
     * @param quantity units to deduct
     */
    public void decreaseStock(int quantity) {
        if (quantity > stockQuantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for '" + name + "'. Available: " + stockQuantity
                            + ", Requested: " + quantity);
        }
        this.stockQuantity -= quantity;
    }

    /**
     * Increases the stock by the given quantity (used on order cancellation / restocking).
     *
     * @param quantity units to add back
     */
    public void increaseStock(int quantity) {
        if (quantity <= 0) throw new InvalidOperationException("Restock quantity must be positive.");
        this.stockQuantity += quantity;
    }

    /** Returns {@code true} if the product has at least one unit in stock. */
    public boolean isInStock() {
        return stockQuantity > 0;
    }

    // -----------------------------------------------------------------------
    // Getters & Setters
    // -----------------------------------------------------------------------

    public String getProductId()               { return productId; }

    public String getName()                    { return name; }
    public void   setName(String name)         { this.name = name; }

    public String getCategory()                { return category; }
    public void   setCategory(String category) { this.category = category; }

    public double getPrice()                   { return price; }
    public void   setPrice(double price) {
        if (price < 0) throw new InvalidOperationException("Price cannot be negative.");
        this.price = price;
    }

    public int    getStockQuantity()           { return stockQuantity; }

    // -----------------------------------------------------------------------
    // Object overrides
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', category='%s', price=INR %.2f, stock=%d}",
                productId, name, category, price, stockQuantity);
    }
}
