package com.quickcommerce.service;

import com.quickcommerce.model.Product;
import com.quickcommerce.persistence.entity.ProductEntity;

/**
 * Mapping responsibilities extracted from ProductService to respect SRP
 * and to keep persistence concerns separated from business logic.
 */
public final class ProductMapper {
    private ProductMapper() {}

    public static Product fromEntity(ProductEntity e) {
        return new Product(e.getProductId(), e.getName(), e.getCategory(), e.getPrice(), e.getStockQuantity());
    }

    public static ProductEntity toEntity(Product p) {
        ProductEntity e = new ProductEntity();
        e.setProductId(p.getProductId());
        e.setName(p.getName());
        e.setCategory(p.getCategory());
        e.setPrice(p.getPrice());
        e.setStockQuantity(p.getStockQuantity());
        return e;
    }
}
