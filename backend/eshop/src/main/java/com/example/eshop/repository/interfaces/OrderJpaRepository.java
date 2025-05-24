package com.example.eshop.repository.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.eshop.model.Order;

@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Integer> {
    
}
