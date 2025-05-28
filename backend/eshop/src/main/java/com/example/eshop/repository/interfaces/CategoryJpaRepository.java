package com.example.eshop.repository.interfaces;

import com.example.eshop.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryJpaRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByCategoryName(String categoryName);
    
    List<Category> findByCategoryNameContainingIgnoreCase(String categoryName);
    
    @Query("SELECT c FROM Category c WHERE c.categoryId IN :ids")
    List<Category> findByIdIn(@Param("ids") List<Long> ids);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.categoryId = :categoryId")
    Long countProductsByCategoryId(@Param("categoryId") Long categoryId);
    
    boolean existsByCategoryName(String categoryName);
    
    // Convenience methods for compatibility
    default Optional<Category> findByName(String name) {
        return findByCategoryName(name);
    }
    
    default boolean existsByName(String name) {
        return existsByCategoryName(name);
    }
}
