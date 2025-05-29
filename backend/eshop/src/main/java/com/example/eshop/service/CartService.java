package com.example.eshop.service;

import com.example.eshop.exception.*;
import com.example.eshop.model.CartItem;
import com.example.eshop.model.Product;
import com.example.eshop.model.User;
import com.example.eshop.model.dto.business.AddToCartRequest;
import com.example.eshop.model.dto.business.CartItemDto;
import com.example.eshop.model.dto.business.ProductDto;
import com.example.eshop.model.dto.business.UserCartDto;
import com.example.eshop.repository.interfaces.ProductJpaRepository;
import com.example.eshop.repository.interfaces.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final UserJpaRepository userRepo;
    private final ProductJpaRepository productRepo;

    public UserCartDto getUserCart(Long userId) {
        User user = findUserById(userId);
        return mapToUserCartResponseDto(user);
    }

    public void addItemToCart(Long userId, AddToCartRequest request) {
        User user = findUserById(userId);
        Product product = findProductById(request.productId());

        validateStockAvailability(product, request.quantity());

        Optional<CartItem> existingItem = user.getCartItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(product.getProductId()))
                .findFirst();

        existingItem.ifPresentOrElse(
                item -> {
                    int newQuantity = item.getQuantity() + request.quantity();
                    validateStockAvailability(product, newQuantity);
                    item.setQuantity(newQuantity);
                },
                () -> {
                    CartItem newItem = new CartItem();
                    newItem.setUser(user);
                    newItem.setProduct(product);
                    newItem.setQuantity(request.quantity());
                    user.getCartItems().add(newItem);
                }
        );

        userRepo.save(user);
    }

    public void updateCartItem(Long userId, Long productId, Integer quantity) {
        User user = findUserById(userId);
        CartItem item = user.getCartItems().stream()
                .filter(ci -> ci.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(user.getId(), productId));

        validateStockAvailability(item.getProduct(), quantity);

        item.setQuantity(quantity);
        userRepo.save(user);
    }

    public void removeCartItem(Long userId, Long productId) {
        User user = findUserById(userId);

        boolean removed = user.getCartItems().removeIf(
                item -> item.getProduct().getProductId().equals(productId)
        );

        if (!removed) {
            throw new CartItemNotFoundException(userId, productId);
        }

        userRepo.save(user);
    }

    public void clearCart(Long userId) {
        User user = findUserById(userId);
        user.getCartItems().clear();
        userRepo.save(user);
    }

    // --- Private Helper Methods ---

    private User findUserById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Product findProductById(Long productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(Collections.singletonList(productId)));
    }

    private void validateStockAvailability(Product product, Integer requestedQuantity) {
        if (!product.getIsActive()) {
            throw new ProductNotAvailableException(product.getProductId());
        }

        if (product.getStock() < requestedQuantity) {
            throw new InsufficientProductStockException(product.getProductId(), product.getStock());
        }
    }

    private BigDecimal calculateCartTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // --- Mapping Methods ---

    private UserCartDto mapToUserCartResponseDto(User user) {
        List<CartItemDto> cartItemDtos = user.getCartItems().stream()
                .map(this::toCartItemDto)
                .toList();
        BigDecimal totalPrice = calculateCartTotal(user.getCartItems());

        return new UserCartDto(cartItemDtos, totalPrice);
    }

    private CartItemDto toCartItemDto(CartItem item) {
        ProductDto productDto = toProductDto(item.getProduct());
        boolean isAvailable = (item.getQuantity() <= productDto.stockQuantity()) && productDto.isActive();

        return new CartItemDto(
                item.getCartItemId(),
                productDto,
                item.getQuantity(),
                isAvailable
        );
    }    private ProductDto toProductDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.getStockQuantity(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getIsActive()
        );
    }
}
