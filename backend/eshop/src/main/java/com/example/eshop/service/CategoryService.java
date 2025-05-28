package com.example.eshop.service;

import com.example.eshop.exception.CategoryNotFoundException;
import com.example.eshop.exception.DuplicateCategoryException;
import com.example.eshop.model.Category;
import com.example.eshop.repository.interfaces.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryJpaRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        log.debug("Fetching all categories");
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        log.debug("Fetching category by id: {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Category getCategoryByName(String name) {
        log.debug("Fetching category by name: {}", name);
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with name: " + name));
    }

    @Transactional(readOnly = true)
    public List<Category> searchCategories(String searchTerm) {
        log.debug("Searching categories with term: {}", searchTerm);
        return categoryRepository.findByCategoryNameContainingIgnoreCase(searchTerm);
    }

    public Category createCategory(Category category) {
        log.debug("Creating new category: {}", category.getName());
        
        if (categoryRepository.existsByName(category.getName())) {
            throw new DuplicateCategoryException("Category already exists with name: " + category.getName());
        }
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Created category with id: {}", savedCategory.getId());
        return savedCategory;
    }

    public Category updateCategory(Long id, Category categoryDetails) {
        log.debug("Updating category with id: {}", id);
        
        Category existingCategory = getCategoryById(id);
        
        // Check if name is being changed and if new name already exists
        if (!existingCategory.getName().equals(categoryDetails.getName()) && 
            categoryRepository.existsByName(categoryDetails.getName())) {
            throw new DuplicateCategoryException("Category already exists with name: " + categoryDetails.getName());
        }
        
        existingCategory.setName(categoryDetails.getName());
        existingCategory.setDescription(categoryDetails.getDescription());
        
        Category updatedCategory = categoryRepository.save(existingCategory);
        log.info("Updated category with id: {}", updatedCategory.getId());
        return updatedCategory;
    }

    public void deleteCategory(Long id) {
        log.debug("Deleting category with id: {}", id);
        
        Category category = getCategoryById(id);
        
        // Check if category has associated products
        Long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException("Cannot delete category with " + productCount + " associated products");
        }
        
        categoryRepository.delete(category);
        log.info("Deleted category with id: {}", id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    @Transactional(readOnly = true)
    public Long getProductCountByCategory(Long categoryId) {
        return categoryRepository.countProductsByCategoryId(categoryId);
    }
}
