package com.example.eshop.controller;

import com.example.eshop.exception.MissingImageException;
import com.example.eshop.model.Category;
import com.example.eshop.model.Product;
import com.example.eshop.model.User;
import com.example.eshop.model.dto.business.ProductDto;
import com.example.eshop.model.dto.business.ProductRequest;
import com.example.eshop.service.CategoryService;
import com.example.eshop.service.ImageUploadService;
import com.example.eshop.service.ProductService;
import com.example.eshop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Management", description = "Administrative functions for managing products, categories, and users")
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final ImageUploadService imageUploadService;

    // Product Management Endpoints

    @PostMapping("/products")
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductDto> createProduct(
            @Valid @ModelAttribute ProductRequest productRequest) {

        log.debug("Admin creating new product: {}", productRequest.name());

        // Image is require for creating new product
        if (productRequest.image() == null || productRequest.image().isEmpty()){
            throw new MissingImageException(productRequest.name());
        }

        String imageUrl = imageUploadService.uploadImage(productRequest.image(), productRequest.name());

        ProductDto productDto = new ProductDto(null ,productRequest.name(), productRequest.description(), productRequest.price(), imageUrl, productRequest.stockQuantity(), productRequest.categoryId(), productRequest.categoryName(), productRequest.isActive()); // Since createProduct use ProductDto has image as url not file

        Product createdProduct = productService.createProduct(productDto);
        ProductDto responseDto = productService.convertToDto(createdProduct);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update an existing product")
    public ResponseEntity<ProductDto> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Valid @ModelAttribute ProductRequest productRequest) {
        
        log.debug("Admin updating product with id: {}", id);

        Product existingProduct = productService.getProductById(id);
        String currentName = existingProduct.getProductName();
        String newName = productRequest.name();

        String newImageUrl = existingProduct.getImageUrl(); // Initiate newImageUrl with existing one

        // If change product name
        if (!currentName.equalsIgnoreCase(newName)){
            String getImage = imageUploadService.renameImage(currentName, newName);

            if (getImage != null){
                newImageUrl = getImage;
            }
        }

        // If upload new image
        if (productRequest.image() != null && !productRequest.image().isEmpty()) {
            newImageUrl = imageUploadService.uploadImage(productRequest.image(), newName);
        }

        ProductDto productDto = new ProductDto(productRequest.id(), productRequest.name(), productRequest.description(), productRequest.price(), newImageUrl, productRequest.stockQuantity(), productRequest.categoryId(), productRequest.categoryName(), productRequest.isActive()); // Since createProduct use ProductDto has image as url not file

        Product updatedProduct = productService.updateProduct(id, productDto);
        ProductDto responseDto = productService.convertToDto(updatedProduct);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        
        log.debug("Admin deleting product with id: {}", id);

        Product product = productService.getProductById(id);
        imageUploadService.deleteImage(product.getProductName());

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/products/{id}/stock")
    @Operation(summary = "Update product stock quantity")
    public ResponseEntity<ProductDto> updateProductStock(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "New stock quantity") @RequestParam Integer stockQuantity) {
        
        log.debug("Admin updating stock for product id: {} to quantity: {}", id, stockQuantity);
        Product updatedProduct = productService.updateStock(id, stockQuantity);
        ProductDto responseDto = productService.convertToDto(updatedProduct);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/products")
    @Operation(summary = "Get all products for admin management")
    public ResponseEntity<Page<ProductDto>> getAllProductsForAdmin(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.debug("Admin fetching all products with pagination");
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage = productService.getAllProducts(pageable);
        Page<ProductDto> productDtoPage = productPage.map(productService::convertToDto);
        return ResponseEntity.ok(productDtoPage);
    }

    // Category Management Endpoints
    
    @PostMapping("/categories")
    @Operation(summary = "Create a new category")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        log.debug("Admin creating new category: {}", category.getName());
        Category createdCategory = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update an existing category")
    public ResponseEntity<Category> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Valid @RequestBody Category categoryDetails) {
        
        log.debug("Admin updating category with id: {}", id);
        Category updatedCategory = categoryService.updateCategory(id, categoryDetails);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete a category")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        
        log.debug("Admin deleting category with id: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories for admin management")
    public ResponseEntity<List<Category>> getAllCategoriesForAdmin() {
        log.debug("Admin fetching all categories");
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{id}/product-count")
    @Operation(summary = "Get product count for a category")
    public ResponseEntity<Long> getCategoryProductCount(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        
        log.debug("Admin fetching product count for category id: {}", id);
        Long productCount = categoryService.getProductCountByCategory(id);
        return ResponseEntity.ok(productCount);
    }

    // User Management Endpoints
    
    @GetMapping("/users")
    @Operation(summary = "Get all users for admin management")
    public ResponseEntity<Page<User>> getAllUsersForAdmin(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.debug("Admin fetching all users with pagination");
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> userPage = userService.getAllUsers(pageable);
        return ResponseEntity.ok(userPage);
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        
        log.debug("Admin fetching user by id: {}", id);
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users/search")
    @Operation(summary = "Search users by email")
    public ResponseEntity<List<User>> searchUsers(
            @Parameter(description = "Email search term") @RequestParam String email) {
        
        log.debug("Admin searching users with email: {}", email);
        List<User> users = userService.searchUsersByEmail(email);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update user role")
    public ResponseEntity<User> updateUserRole(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Parameter(description = "New role") @RequestParam String role) {
        
        log.debug("Admin updating role for user id: {} to role: {}", id, role);
        User updatedUser = userService.updateUserRole(id, role);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user account")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        
        log.debug("Admin deleting user with id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // System Statistics Endpoints
    
    @GetMapping("/stats/users")
    @Operation(summary = "Get total user count")
    public ResponseEntity<Long> getTotalUserCount() {
        log.debug("Admin fetching total user count");
        Long userCount = userService.getTotalUserCount();
        return ResponseEntity.ok(userCount);
    }

    @GetMapping("/stats/products")
    @Operation(summary = "Get total product count")
    public ResponseEntity<Long> getTotalProductCount() {
        log.debug("Admin fetching total product count");
        Page<Product> productPage = productService.getAllProducts(PageRequest.of(0, 1));
        return ResponseEntity.ok(productPage.getTotalElements());
    }

    @GetMapping("/stats/categories")
    @Operation(summary = "Get total category count")
    public ResponseEntity<Long> getTotalCategoryCount() {
        log.debug("Admin fetching total category count");
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok((long) categories.size());
    }
}
