package com.example.eshop.controller;

import com.example.eshop.model.Category;
import com.example.eshop.model.Product;
import com.example.eshop.model.dto.business.ProductDto;
import com.example.eshop.service.CategoryService;
import com.example.eshop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Catalog", description = "Public API for browsing products and categories")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all products with pagination and filtering")
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Category ID filter") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Minimum price filter") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price filter") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Search term") @RequestParam(required = false) String search) {
        
        log.debug("Fetching products - page: {}, size: {}, categoryId: {}, minPrice: {}, maxPrice: {}, search: {}", 
                page, size, categoryId, minPrice, maxPrice, search);

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage;
        
        if (search != null && !search.trim().isEmpty()) {
            productPage = productService.searchProducts(search.trim(), pageable);
        } else if (categoryId != null && minPrice != null && maxPrice != null) {
            productPage = productService.getProductsByCategoryAndPriceRange(categoryId, minPrice, maxPrice, pageable);
        } else if (categoryId != null) {
            productPage = productService.getProductsByCategory(categoryId, pageable);
        } else if (minPrice != null && maxPrice != null) {
            productPage = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        } else {
            productPage = productService.getAllProducts(pageable);
        }
        
        Page<ProductDto> productDtoPage = productPage.map(productService::convertToDto);
        return ResponseEntity.ok(productDtoPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductDto> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        
        log.debug("Fetching product by id: {}", id);
        Product product = productService.getProductById(id);
        ProductDto productDto = productService.convertToDto(product);
        return ResponseEntity.ok(productDto);
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products")
    public ResponseEntity<List<ProductDto>> getFeaturedProducts() {
        log.debug("Fetching featured products");
        List<Product> featuredProducts = productService.getFeaturedProducts();
        List<ProductDto> featuredProductDtos = featuredProducts.stream()
                .map(productService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(featuredProductDtos);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<Page<ProductDto>> getProductsByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.debug("Fetching products by category id: {} with pagination", categoryId);
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage = productService.getProductsByCategory(categoryId, pageable);
        Page<ProductDto> productDtoPage = productPage.map(productService::convertToDto);
        return ResponseEntity.ok(productDtoPage);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name or description")
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @Parameter(description = "Search term") @RequestParam String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.debug("Searching products with term: {}", q);
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage = productService.searchProducts(q, pageable);
        Page<ProductDto> productDtoPage = productPage.map(productService::convertToDto);
        return ResponseEntity.ok(productDtoPage);
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        log.debug("Fetching all categories");
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<Category> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        
        log.debug("Fetching category by id: {}", id);
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/categories/search")
    @Operation(summary = "Search categories by name")
    public ResponseEntity<List<Category>> searchCategories(
            @Parameter(description = "Search term") @RequestParam String q) {
        
        log.debug("Searching categories with term: {}", q);
        List<Category> categories = categoryService.searchCategories(q);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/check-stock/{id}")
    @Operation(summary = "Check product stock availability")
    public ResponseEntity<Boolean> checkProductStock(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "Requested quantity") @RequestParam Integer quantity) {
        
        log.debug("Checking stock for product id: {} with quantity: {}", id, quantity);
        boolean inStock = productService.isProductInStock(id, quantity);
        return ResponseEntity.ok(inStock);
    }
}
