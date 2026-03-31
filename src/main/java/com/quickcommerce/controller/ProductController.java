package com.quickcommerce.controller;

import java.util.List;

import com.quickcommerce.model.Product;
import com.quickcommerce.service.IProductService;

/**
 * ProductController — thin controller coordinating UI actions to the product domain.
 *
 * GRASP: Controller - receives UI events and delegates to the product service (Information Expert).
 * SOLID: - SRP: only coordinates product use-cases.
 *        - DIP: depends on `IProductService` abstraction, not concrete implementation.
 */
public class ProductController {

    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    public List<Product> getAllProducts() { return productService.getAllProducts(); }

    public void addProduct(Product p) { productService.addProduct(p); }

    public void removeProduct(String productId) { productService.removeProduct(productId); }

    public Product findById(String id) { return productService.findById(id); }
}
