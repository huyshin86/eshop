package com.example.eshop.controller;

import com.example.eshop.exception.UserNotFoundException;
import com.example.eshop.model.CartItem;
import com.example.eshop.model.Product;
import com.example.eshop.model.User;
import com.example.eshop.model.dto.business.ProductDto;
import com.example.eshop.model.dto.business.UserCartDto;
import com.example.eshop.model.dto.business.CartItemDto;
import com.example.eshop.model.dto.common.SuccessResponse;
import com.example.eshop.repository.interfaces.UserRepository;
import com.example.eshop.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getUserCart(){
        Long userId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        UserCartDto userCartDto = mapToUserCartResponseDto(user);
        return ResponseEntity.ok(
                new SuccessResponse<>(userCartDto)
        );
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
