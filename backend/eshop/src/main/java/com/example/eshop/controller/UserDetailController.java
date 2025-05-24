package com.example.eshop.controller;

import java.util.List;

import com.example.eshop.exception.UserNotFoundException;
import com.example.eshop.model.dto.business.response.UserOrderResponseDto;
import com.example.eshop.model.dto.common.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.eshop.model.Order;
import com.example.eshop.model.OrderItem;
import com.example.eshop.model.User;
import com.example.eshop.model.dto.business.response.OrderItemResponseDto;
import com.example.eshop.model.dto.business.response.OrderResponseDto;
import com.example.eshop.model.dto.business.response.UserInfoResponseDto;
import com.example.eshop.repository.interfaces.UserRepository;
import com.example.eshop.security.util.CheckResourceOwnership;
import com.example.eshop.security.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserDetailController {
    private final UserRepository userRepository;

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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return ResponseEntity.ok(
                new SuccessResponse<>(mapToUserInfoResponseDto(user))
        );
    }

    @GetMapping("/me/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getUserOrders() {
        Long userId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return ResponseEntity.ok(
                new SuccessResponse<>(maptoUserOrderResponseDto(user))
        );
    }

    private UserInfoResponseDto mapToUserInfoResponseDto(User user) {
        return new UserInfoResponseDto(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getAddress());
    }

    private UserOrderResponseDto maptoUserOrderResponseDto(User user){
        return new UserOrderResponseDto(mapOrders(user.getOrders()));
    }

    private List<OrderResponseDto> mapOrders(List<Order> orders) {
        if (orders == null) {
            return List.of();
        }
        return orders.stream()
            .map(this::toOrderResponseDto)
            .toList();
    }

    private OrderResponseDto toOrderResponseDto(Order order) {
        if (order == null) {
            return null;
        }
        return new OrderResponseDto(
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
    private List<OrderItemResponseDto> mapOrderItems(List<OrderItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
            .map(this::toOrderItemResponseDto)
            .toList();
    }

    private OrderItemResponseDto toOrderItemResponseDto(OrderItem item) {
        return new OrderItemResponseDto(
            item.getOrderItemId(),
            item.getProduct().getProductId(),
            item.getProduct().getProductName(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getTotal()
        );
    }
}
