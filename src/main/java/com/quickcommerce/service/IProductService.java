package com.quickcommerce.service;

import java.util.List;

import com.quickcommerce.model.Product;

/**
 * Service interface for product operations — used to depend on abstractions
 * (follows Dependency Inversion). Implementations should focus on domain
 * responsibilities; mapping/presentation should be extracted to helpers.
 */
public interface IProductService {
    List<Product> getAllProducts();
    void addProduct(Product p);
    void removeProduct(String productId);
    Product findById(String id);
    List<Product> searchByName(String keyword);
    List<Product> getByCategory(String category);
    List<Product> getInStockProducts();
}
