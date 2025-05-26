package com.example.eshop.controller;

import java.util.List;

import com.example.eshop.model.dto.business.*;
import com.example.eshop.model.dto.common.SuccessResponse;
import com.example.eshop.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.eshop.security.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor

public class UserDetailController {

    private final UserService userService;

    @GetMapping("/me/details")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> getUserDetails() {
        Long userId = SecurityUtils.getCurrentUserId();

        UserInfoDto userInfo = userService.getUserInfo(userId);

        return ResponseEntity.ok(
                new SuccessResponse<>(userInfo)
        );
    }

    @GetMapping("/me/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getUserOrders() {
        Long userId = SecurityUtils.getCurrentUserId();

        UserOrderDto userOrders = userService.getUserOrders(userId);

        return ResponseEntity.ok(
                new SuccessResponse<>(userOrders)
        );
    }

    @GetMapping("/me/orders/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getOrderDetails(
            @PathVariable
            @NotNull(message = "Product ID is required")
            @Positive(message = "Product ID must be positive") Long orderId
    ) {
        Long userId = SecurityUtils.getCurrentUserId();

        OrderDto order = userService.getOrderById(userId, orderId);

        return ResponseEntity.ok(
                new SuccessResponse<>(order)
        );
    }

    @GetMapping("/me/orders/status")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getOrdersByStatus(
            @RequestParam
            @NotBlank(message = "Status is required") String status) {
        Long userId = SecurityUtils.getCurrentUserId();

        List<OrderDto> orders = userService.getOrdersByStatus(userId, status);

        return ResponseEntity.ok(
                new SuccessResponse<>(orders)
        );
    }

    @PutMapping("/me/details")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> updateUserDetails(@Valid @RequestBody UpdateUserInfoRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        UserInfoDto updatedUser = userService.updateUserInfo(userId, request);

        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK, "User information updated successfully")
        );
    }
}
