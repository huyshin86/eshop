package com.example.eshop.service;

import com.example.eshop.exception.CategoryNotFoundException;
import com.example.eshop.exception.ProductNotFoundException;
import com.example.eshop.model.Category;
import com.example.eshop.model.Product;
import com.example.eshop.model.dto.business.ProductDto;
import com.example.eshop.repository.interfaces.CategoryJpaRepository;
import com.example.eshop.repository.interfaces.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductJpaRepository productRepository;
    private final CategoryJpaRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products with pagination: {}", pageable);
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        log.debug("Fetching product by id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.debug("Fetching products by category id: {} with pagination: {}", categoryId, pageable);
        
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category not found with id: " + categoryId);
        }
        
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String searchTerm, Pageable pageable) {
        log.debug("Searching products with term: {} and pagination: {}", searchTerm, pageable);
        return productRepository.findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                searchTerm, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.debug("Fetching products by price range: {} - {} with pagination: {}", minPrice, maxPrice, pageable);
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategoryAndPriceRange(Long categoryId, BigDecimal minPrice, 
                                                           BigDecimal maxPrice, Pageable pageable) {
        log.debug("Fetching products by category: {} and price range: {} - {} with pagination: {}", 
                categoryId, minPrice, maxPrice, pageable);
        
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category not found with id: " + categoryId);
        }
        
        return productRepository.findByCategoryIdAndPriceBetween(categoryId, minPrice, maxPrice, pageable);
    }

    @Transactional(readOnly = true)
    public List<Product> getFeaturedProducts() {
        log.debug("Fetching featured products");
        return productRepository.findTop8ByOrderByProductIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByIds(List<Long> productIds) {
        log.debug("Fetching products by ids: {}", productIds);
        return productRepository.findAllById(productIds);
    }

    public Product createProduct(ProductDto productDto) {
        log.debug("Creating new product: {}", productDto.name());
        
        Category category = categoryRepository.findById(productDto.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + productDto.categoryId()));
        
        Product product = Product.builder()
                .productName(productDto.name())
                .description(productDto.description())
                .price(productDto.price())
                .stock(productDto.stockQuantity())
                .imageUrl(productDto.imageUrl())
                .category(category)
                .build();
        
        Product savedProduct = productRepository.save(product);
        log.info("Created product with id: {}", savedProduct.getId());
        return savedProduct;
    }

    public Product updateProduct(Long id, ProductDto productDto) {
        log.debug("Updating product with id: {}", id);
        
        Product existingProduct = getProductById(id);
        
        Category category = categoryRepository.findById(productDto.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + productDto.categoryId()));
        
        existingProduct.setName(productDto.name());
        existingProduct.setDescription(productDto.description());
        existingProduct.setPrice(productDto.price());
        existingProduct.setStockQuantity(productDto.stockQuantity());
        existingProduct.setImageUrl(productDto.imageUrl());
        existingProduct.setCategory(category);
        
        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Updated product with id: {}", updatedProduct.getId());
        return updatedProduct;
    }

    public void deleteProduct(Long id) {
        log.debug("Deleting product with id: {}", id);
        
        Product product = getProductById(id);
        productRepository.delete(product);
        log.info("Deleted product with id: {}", id);
    }

    public Product updateStock(Long id, Integer stockQuantity) {
        log.debug("Updating stock for product id: {} to quantity: {}", id, stockQuantity);
        
        Product product = getProductById(id);
        product.setStockQuantity(stockQuantity);
        
        Product updatedProduct = productRepository.save(product);
        log.info("Updated stock for product id: {} to quantity: {}", id, stockQuantity);
        return updatedProduct;
    }

    @Transactional(readOnly = true)
    public boolean isProductInStock(Long productId, Integer requestedQuantity) {
        Product product = getProductById(productId);
        return product.getStockQuantity() >= requestedQuantity;
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public ProductDto convertToDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.getStockQuantity(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getIsActive()
        );
    }
}
