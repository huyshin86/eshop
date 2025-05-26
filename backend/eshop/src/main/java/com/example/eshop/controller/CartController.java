package com.example.eshop.controller;

import com.example.eshop.exception.CartItemNotFoundException;
import com.example.eshop.exception.ProductNotFoundException;
import com.example.eshop.exception.UserNotFoundException;
import com.example.eshop.model.CartItem;
import com.example.eshop.model.Product;
import com.example.eshop.model.User;
import com.example.eshop.model.dto.business.AddToCartRequest;
import com.example.eshop.model.dto.business.ProductDto;
import com.example.eshop.model.dto.business.UserCartDto;
import com.example.eshop.model.dto.business.CartItemDto;
import com.example.eshop.model.dto.common.SuccessResponse;
import com.example.eshop.repository.interfaces.ProductJpaRepository;
import com.example.eshop.repository.interfaces.UserJpaRepository;
import com.example.eshop.security.util.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final UserJpaRepository userJpaRepository;
    private final ProductJpaRepository productJpaRepository;

    @GetMapping
    public ResponseEntity<?> getUserCart(){
        Long userId = SecurityUtils.getCurrentUserId();

        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        UserCartDto userCartDto = mapToUserCartResponseDto(user);
        return ResponseEntity.ok(
                new SuccessResponse<>(userCartDto)
        );
    }

    @PostMapping
    public ResponseEntity<?> addCartItem(@Valid @RequestBody AddToCartRequest request){
        Long userId = SecurityUtils.getCurrentUserId();
        Long productId = request.productId();

        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        CartItem existingItem = user.getCartItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(product.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.quantity());
        } else {
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(request.quantity());

            user.getCartItems().add(newItem);
        }

        userJpaRepository.save(user);

        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Product added to cart"));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable  @NotNull(message = "Product ID is required")
                            @Positive(message = "Product ID must be positive") Long productId,
            @RequestParam  @NotNull(message = "Quantity is required")
                            @Positive(message = "Quantity must be at least 1") Integer quantity
    ) {

        Long userId = SecurityUtils.getCurrentUserId();
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        CartItem item = user.getCartItems().stream()
                .filter(ci -> ci.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(userId, productId));

        item.setQuantity(quantity);
        userJpaRepository.save(user);

        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK,"Cart item updated"));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeCartItem(
            @PathVariable @NotNull(message = "Product ID is required")
                            @Positive(message = "Product ID must be positive") Long productId
    ) {

        Long userId = SecurityUtils.getCurrentUserId();
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        boolean removed = user.getCartItems().removeIf(
                item -> item.getProduct().getProductId().equals(productId)
        );

        if (!removed) {
            throw new CartItemNotFoundException(userId, productId);
        }

        userJpaRepository.save(user);

        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK,"Cart item removed"));
    }

    private UserCartDto mapToUserCartResponseDto(User user){
        List<CartItemDto> cartItemDtos = mapCartItems(user.getCartItems());

        BigDecimal totalPrice = cartItemDtos.stream()
                .map(item -> item.product().price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new UserCartDto(cartItemDtos, totalPrice);
    }

    private List<CartItemDto> mapCartItems(List<CartItem> items){
        if (items == null){
            return List.of();
        }

        return items.stream()
                .map(this::toCartItemDto)
                .toList();
    }

    private CartItemDto toCartItemDto (CartItem item){
        ProductDto productDto = toProductDto(item.getProduct());
        boolean isAvailable = (item.getQuantity() <= productDto.stock()) & productDto.isActive();

        return new CartItemDto(
                item.getCartItemId(),
                productDto,
                item.getQuantity(),
                isAvailable
        );
    }

    private ProductDto toProductDto(Product product){
        return new ProductDto(
                product.getProductId(),
                product.getProductName(),
                null,
                product.getPrice(),
                product.getImageUrl(),
                product.getStock(),
                product.getIsActive()
        );
    }
}
