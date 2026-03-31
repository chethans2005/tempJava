package com.quickcommerce.model;

import com.quickcommerce.enums.OrderStatus;
import com.quickcommerce.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Vendor (seller/store) who lists products and
 * fulfils incoming orders on the Quick Commerce platform.
 *
 * <p>Relationship summary (from UML):
 * <ul>
 *   <li>Vendor → Product : 1-to-many (manages product catalogue)</li>
 * </ul>
 * </p>
 */
public class Vendor extends User {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private String              vendorId;
    private String              storeName;
    private final List<Product> productCatalogue;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a Vendor.
     *
     * @param name      full name of the vendor/owner
     * @param email     e-mail address
     * @param password  password
     * @param storeName name of the store
     */
    public Vendor(String name, String email, String password, String storeName) {
        super(name, email, password);
        this.vendorId         = "VND-" + getUserId().substring(0, 8).toUpperCase();
        this.storeName        = storeName;
        this.productCatalogue = new ArrayList<>();
    }

    // -----------------------------------------------------------------------
    // Business methods — Product management
    // -----------------------------------------------------------------------

    /**
     * Adds a new product to this vendor's catalogue.
     *
     * @param product the product to add
     */
    public void addProduct(Product product) {
        productCatalogue.add(product);
        System.out.println("[VENDOR] Product '" + product.getName() + "' added by " + storeName);
    }

    /**
     * Updates an existing product's price and stock quantity.
     *
     * @param productId       ID of the product to update
     * @param newPrice        updated price
     * @param newStockQuantity updated stock quantity
     */
    public void updateProduct(String productId, double newPrice, int newStockQuantity) {
        Product product = findProductById(productId);
        product.setPrice(newPrice);
        product.updateStock(newStockQuantity);
        System.out.println("[VENDOR] Product '" + product.getName() + "' updated.");
    }

    /**
     * Removes a product from this vendor's catalogue.
     *
     * @param productId the ID of the product to remove
     */
    public void removeProduct(String productId) {
        Product product = findProductById(productId);
        productCatalogue.remove(product);
        System.out.println("[VENDOR] Product '" + product.getName() + "' removed.");
    }

    /**
     * Processes (confirms) an incoming order that involves this vendor's products.
     *
     * @param order the order to process
     */
    public void processOrder(Order order) {
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
            System.out.println("[VENDOR] " + storeName + " confirmed Order " + order.getOrderId());
        } else {
            System.out.println("[VENDOR] Order " + order.getOrderId()
                    + " cannot be processed — current status: " + order.getStatus());
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Product findProductById(String productId) {
        return productCatalogue.stream()
                .filter(p -> p.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found in catalogue: " + productId));
    }

    // -----------------------------------------------------------------------
    // Getters & Setters
    // -----------------------------------------------------------------------

    public String        getVendorId()         { return vendorId; }

    public String        getStoreName()         { return storeName; }
    public void          setStoreName(String n) { this.storeName = n; }

    public List<Product> getProductCatalogue()  {
        return Collections.unmodifiableList(productCatalogue);
    }

    // -----------------------------------------------------------------------
    // User overrides
    // -----------------------------------------------------------------------

    @Override
    public String getRole() {
        return "Vendor";
    }

    @Override
    public String toString() {
        return String.format("Vendor{vendorId='%s', storeName='%s', products=%d}",
                vendorId, storeName, productCatalogue.size());
    }
}
