package com.example.eshop.repository.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.eshop.model.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByPaypalOrderId(String paypalOrderId);
    List<Order> findByOrderStatusAndOrderDateBefore(Order.OrderStatus orderStatus, LocalDateTime cutoff);
}
