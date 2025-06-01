package com.example.eshop.repository.interfaces;

import com.example.eshop.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.productId IN :ids")
    List<Product> findAllByIdForUpdate(@Param("ids") List<Long> ids);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "UPPER(p.productName) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
            "p.description LIKE CONCAT('%', :searchTerm, '%')")
    Page<Product> findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            @Param("searchTerm") String searchTerm, Pageable pageable);

    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<Product> findByCategoryIdAndPriceBetween(Long categoryId, BigDecimal minPrice,
                                                  BigDecimal maxPrice, Pageable pageable);

    List<Product> findTop8ByOrderByProductIdDesc();

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);
}