package com.quickcommerce.service;

import java.util.List;

import com.quickcommerce.model.Product;

/**
 * Presentation helper extracted from ProductService. Services should not
 * handle console/UI output; moving presentation respects SRP and keeps
 * responsibilities separated.
 */
public final class ProductPresenter {
    private ProductPresenter() {}

    public static void displayAllProducts(List<Product> catalogue) {
        if (catalogue == null || catalogue.isEmpty()) {
            System.out.println("[PRODUCT-PRESENTER] No products listed yet.");
            return;
        }
        System.out.println("\n[PRODUCT-PRESENTER] === Full Product Catalogue ===");
        System.out.printf("%-15s %-25s %-15s %8s %8s%n",
                "Product ID", "Name", "Category", "Price", "Stock");
        System.out.println("-".repeat(75));
        catalogue.forEach(p -> System.out.printf("%-15s %-25s %-15s %8.2f %8d%n",
                p.getProductId(), p.getName(), p.getCategory(), p.getPrice(), p.getStockQuantity()));
    }
}
