package com.example.eshop.controller;

import java.util.List;

import com.example.eshop.exception.UserNotFoundException;
import com.example.eshop.model.Product;
import com.example.eshop.model.dto.business.*;
import com.example.eshop.model.dto.common.SuccessResponse;
import com.example.eshop.repository.interfaces.UserJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.eshop.model.Order;
import com.example.eshop.model.OrderItem;
import com.example.eshop.model.User;
import com.example.eshop.security.util.CheckResourceOwnership;
import com.example.eshop.security.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserDetailController {
    private final UserJpaRepository userJpaRepository;

    // This endpoint is for testing CheckResourceOwnership
    @GetMapping("/{id}/details")
    @PreAuthorize("hasRole('ADMIN')")
    @CheckResourceOwnership
    public String getUserDetails(@PathVariable Long id) {
        return "User details";
    }

    @GetMapping("/me/details")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> getUserDetails() {
        Long userId = SecurityUtils.getCurrentUserId();

        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return ResponseEntity.ok(
                new SuccessResponse<>(mapToUserInfoResponseDto(user))
        );
    }

    @GetMapping("/me/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getUserOrders() {
        Long userId = SecurityUtils.getCurrentUserId();

        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return ResponseEntity.ok(
                new SuccessResponse<>(mapToUserOrderResponseDto(user))
        );
    }

    private UserInfoDto mapToUserInfoResponseDto(User user) {
        return new UserInfoDto(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getAddress());
    }

    private UserOrderDto mapToUserOrderResponseDto(User user){
        return new UserOrderDto(mapOrders(user.getOrders()));
    }

    private List<OrderDto> mapOrders(List<Order> orders) {
        if (orders == null) {
            return List.of();
        }
        return orders.stream()
            .map(this::toOrderResponseDto)
            .toList();
    }

    private OrderDto toOrderResponseDto(Order order) {
        if (order == null) {
            return null;
        }
        return new OrderDto(
            order.getOrderId(),
            order.getOrderNumber(),
            order.getOrderDate(),
            order.getOrderStatus().toString(),
            order.getSubtotal(),
            order.getDiscountAmount(),
            order.getShippingCost(),
            order.getTax(),
            order.getGrandTotal(),
            order.getShippingAddress(),
            mapOrderItems(order.getOrderItems())
        );
    }
    private List<OrderItemDto> mapOrderItems(List<OrderItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
            .map(this::toOrderItemResponseDto)
            .toList();
    }

    private OrderItemDto toOrderItemResponseDto(OrderItem item) {
        return new OrderItemDto(
            item.getOrderItemId(),
            toProductDto(item.getProduct()),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getTotal()
        );
    }
    private ProductDto toProductDto(Product product){
        return new ProductDto(
                product.getProductId(),
                product.getProductName(),
                null,
                null,
                product.getImageUrl(),
                null,
                null
        );
    }
}
