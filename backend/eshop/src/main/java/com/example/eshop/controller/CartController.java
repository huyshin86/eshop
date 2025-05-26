package com.example.eshop.controller;

import com.example.eshop.model.dto.business.AddToCartRequest;
import com.example.eshop.model.dto.business.UserCartDto;
import com.example.eshop.model.dto.common.SuccessResponse;
import com.example.eshop.security.util.SecurityUtils;
import com.example.eshop.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<?> getUserCart() {
        Long userId = SecurityUtils.getCurrentUserId();

        UserCartDto userCartDto = cartService.getUserCart(userId);

        return ResponseEntity.ok(
                new SuccessResponse<>(userCartDto)
        );
    }

    @PostMapping
    public ResponseEntity<?> addCartItem(@Valid @RequestBody AddToCartRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        cartService.addItemToCart(userId, request);

        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK, "Product added to cart")
        );
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable
            @NotNull(message = "Product ID is required")
            @Positive(message = "Product ID must be positive") Long productId,

            @RequestParam
            @NotNull(message = "Quantity is required")
            @Positive(message = "Quantity must be at least 1") Integer quantity
    ) {
        Long userId = SecurityUtils.getCurrentUserId();

        cartService.updateCartItem(userId, productId, quantity);

        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK, "Cart item updated")
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeCartItem(
            @PathVariable
            @NotNull(message = "Product ID is required")
            @Positive(message = "Product ID must be positive") Long productId
    ) {
        Long userId = SecurityUtils.getCurrentUserId();

        cartService.removeCartItem(userId, productId);

        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK, "Cart item removed")
        );
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart() {
        Long userId = SecurityUtils.getCurrentUserId();

        cartService.clearCart(userId);

        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK, "Cart cleared")
        );
    }
}