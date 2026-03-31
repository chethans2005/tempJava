package com.quickcommerce.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.quickcommerce.exception.ResourceNotFoundException;
import com.quickcommerce.model.Product;
import com.quickcommerce.persistence.entity.ProductEntity;
import com.quickcommerce.persistence.repo.ProductRepository;

/**
 * Service layer responsible for the central product catalogue.
 *
 * Supports an optional repository-backed mode when a ProductRepository
 * is supplied (Spring environment). Otherwise falls back to the original
 * in-memory behaviour.
 */
/**
 * ProductService — domain service for product catalogue.
 *
 * GRASP: Information Expert — this class owns the in-memory catalogue and
 * is responsible for business operations on products.
 * SOLID: - SRP: business logic only (mapping and presentation moved out).
 *        - DIP: implements `IProductService` so controllers depend on an abstraction.
 */
public class ProductService implements IProductService {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final List<Product> catalogue;   // Platform-wide product index
    private final ProductRepository productRepository; // nullable

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    public ProductService() {
        this.catalogue = new ArrayList<>();
        this.productRepository = null;
    }

    public ProductService(ProductRepository productRepository) {
        this.catalogue = new ArrayList<>();
        this.productRepository = productRepository;
        try {
            List<ProductEntity> ents = productRepository.findAll();
            for (ProductEntity e : ents) {
                catalogue.add(ProductMapper.fromEntity(e));
            }
        } catch (Exception ignored) {}
    }

    // -----------------------------------------------------------------------
    // CRUD operations
    // -----------------------------------------------------------------------

    @Override
    public void addProduct(Product product) {
        catalogue.add(product);
        if (productRepository != null) {
            try { productRepository.save(toEntity(product)); } catch (Exception ex) {
                System.out.println("[PRODUCT-SERVICE] Warning: failed to persist product: " + ex.getMessage());
            }
        }
        System.out.println("[PRODUCT-SERVICE] Product '" + product.getName() + "' listed on platform.");
    }

    @Override
    public void removeProduct(String productId) {
        Product product = findProductById(productId);
        catalogue.remove(product);
        if (productRepository != null) {
            try { productRepository.deleteById(productId); } catch (Exception ignored) {}
        }
        System.out.println("[PRODUCT-SERVICE] Product '" + product.getName() + "' removed from platform.");
    }

    // -----------------------------------------------------------------------
    // Query methods
    // -----------------------------------------------------------------------

    public Product findProductById(String productId) {
        return catalogue.stream()
                .filter(p -> p.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + productId));
    }

    // Backwards-compatible public API used by callers/controllers
    @Override
    public Product findById(String id) { return findProductById(id); }

    @Override
    public List<Product> searchByName(String keyword) {
        return catalogue.stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> getByCategory(String category) {
        return catalogue.stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> getInStockProducts() {
        return catalogue.stream()
                .filter(Product::isInStock)
                .collect(Collectors.toList());
    }

    // Presentation responsibilities have been moved to ProductPresenter

    /** Returns an unmodifiable view of the full catalogue. */
    @Override
    public List<Product> getAllProducts() {
        return Collections.unmodifiableList(catalogue);
    }

    // Use ProductMapper for entity mapping to respect SRP.
    private ProductEntity toEntity(Product p) { return ProductMapper.toEntity(p); }
}
