package com.quickcommerce.model;

import com.quickcommerce.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a shopping cart belonging to exactly one {@link Customer}.
 *
 * <p>Relationship summary (from UML):
 * <ul>
 *   <li>Cart → CartItem : 1-to-many composition (owns 1..* items)</li>
 *   <li>Customer → Cart : 1-to-1 composition (the cart belongs to one customer)</li>
 * </ul>
 * </p>
 */
public class Cart {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final String         cartId;
    private final Customer       owner;               // back-reference to the owning customer
    private final List<CartItem> cartItems;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates an empty cart for the given customer.
     *
     * @param owner the {@link Customer} who owns this cart
     */
    public Cart(Customer owner) {
        this.cartId    = "CRT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.owner     = owner;
        this.cartItems = new ArrayList<>();
    }

    // -----------------------------------------------------------------------
    // Business methods
    // -----------------------------------------------------------------------

    /**
     * Adds a product to the cart. If the product already exists, its quantity
     * is incremented; otherwise a new {@link CartItem} is created.
     *
     * @param product  the product to add
     * @param quantity units to add (must be >= 1)
     */
    public void addItem(Product product, int quantity) {
        Optional<CartItem> existing = cartItems.stream()
                .filter(ci -> ci.getProduct().getProductId().equals(product.getProductId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
            System.out.println("[CART] Updated quantity for '" + product.getName() + "'.");
        } else {
            cartItems.add(new CartItem(product, quantity));
            System.out.println("[CART] '" + product.getName() + "' added (qty: " + quantity + ").");
        }
    }

    /**
     * Removes the cart item associated with the given product ID.
     *
     * @param productId the ID of the product to remove
     */
    public void removeItem(String productId) {
        CartItem toRemove = cartItems.stream()
                .filter(ci -> ci.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found in cart: " + productId));
        cartItems.remove(toRemove);
        System.out.println("[CART] '" + toRemove.getProduct().getName() + "' removed from cart.");
    }

    /**
     * Calculates the grand total of all items in the cart.
     *
     * @return sum of each {@link CartItem#getTotal()}
     */
    public double calculateTotal() {
        return cartItems.stream().mapToDouble(CartItem::getTotal).sum();
    }

    /** Removes all items from the cart. */
    public void clear() {
        cartItems.clear();
        System.out.println("[CART] Cart cleared.");
    }

    /** Returns {@code true} when the cart has no items. */
    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public String         getCartId()   { return cartId; }
    public Customer       getOwner()    { return owner; }

    /** Returns an unmodifiable view of the cart items. */
    public List<CartItem> getCartItems() {
        return Collections.unmodifiableList(cartItems);
    }

    // -----------------------------------------------------------------------
    // Object overrides
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("Cart{cartId='%s', owner='%s', items=%d, total=INR %.2f}",
                cartId, owner.getName(), cartItems.size(), calculateTotal());
    }
}
