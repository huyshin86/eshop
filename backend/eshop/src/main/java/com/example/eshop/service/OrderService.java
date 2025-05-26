package com.example.eshop.service;

import com.example.eshop.exception.*;
import com.example.eshop.model.*;
import com.example.eshop.model.dto.business.OrderDto;
import com.example.eshop.model.dto.business.OrderItemDto;
import com.example.eshop.model.dto.business.ProductDto;
import com.example.eshop.repository.interfaces.OrderJpaRepository;
import com.example.eshop.repository.interfaces.ProductJpaRepository;
import com.example.eshop.repository.interfaces.UserJpaRepository;
import com.example.eshop.security.util.LockTimeout;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final UserJpaRepository userRepo;
    private final ProductJpaRepository productRepo;
    private final OrderJpaRepository orderRepo;

    @Retryable(
            retryFor = {
                    PessimisticLockException.class,
                    CannotAcquireLockException.class,
                    LockAcquisitionException.class
            },
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    @LockTimeout()
    @Transactional(timeout = 15, rollbackFor = Exception.class)
    public OrderDto checkout(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<CartItem> cartItems = user.getCartItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new CartEmptyException(userId);
        }

        String shippingAddress = user.getAddress();
        if (shippingAddress == null || shippingAddress.isBlank()) {
            throw new ShippingAddressMissingException(userId);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderStatus(Order.OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        order.setShippingAddress(shippingAddress);

        Map<Long, Integer> quantityMap = cartItems.stream()
                .collect(Collectors.toMap(
                        item -> item.getProduct().getProductId(),
                        CartItem::getQuantity
                ));

        List<Long> productIds = new ArrayList<>(quantityMap.keySet());
        List<Product> products;

        products = productRepo.findAllByIdForUpdate(productIds);

        // Check for missing products
        if (products.size() != productIds.size()) {
            Set<Long> foundIds = products.stream()
                    .map(Product::getProductId)
                    .collect(Collectors.toSet());

            List<Long> missingIds = productIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            throw new ProductNotFoundException(missingIds);
        }

        for (Product product : products) {
            Long productId = product.getProductId();
            Integer quantity = quantityMap.get(productId);

            if (!product.getIsActive()) throw new ProductNotAvailableException(productId);
            if (product.getStock() < quantity) throw new InsufficientProductStockException(productId, product.getStock());

            // Decrease stock
            product.setStock(product.getStock() - quantity);

            // Calculate item total
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(itemTotal);

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setTotal(itemTotal);

            order.addOrderItem(orderItem);
        }

        BigDecimal shipping = BigDecimal.valueOf(5);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1));
        BigDecimal grandTotal = subtotal.add(shipping).add(tax);

        order.setSubtotal(subtotal);
        order.setShippingCost(shipping);
        order.setTax(tax);
        order.setGrandTotal(grandTotal);

        // Persist order and update stock
        Order savedOrder = orderRepo.save(order);

        // Clear cart
        clearCart(user);

        return toOrderResponseDto(savedOrder);
    }

    // Helper methods
    private void clearCart(User user) {
        user.getCartItems().clear();
        userRepo.save(user);
    }

    // Mapping methods
    private OrderDto toOrderResponseDto(Order order){
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

    private ProductDto toProductDto(Product product) {
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

    // Catch business exceptions
    @Recover
    public OrderDto recoverFromUserNotFound(UserNotFoundException ex, Long userId) {
        throw ex;  // Re-throw to global handler
    }

    @Recover
    public OrderDto recoverFromCartEmpty(CartEmptyException ex, Long userId) {
        throw ex;  // Re-throw to global handler
    }

    @Recover
    public OrderDto recoverFromShippingMissing(ShippingAddressMissingException ex, Long userId) {
        throw ex;  // Re-throw to global handler
    }

    @Recover
    public OrderDto recoverFromProductNotFound(ProductNotFoundException ex, Long userId) {
        throw ex;  // Re-throw to global handler
    }

    @Recover
    public OrderDto recoverFromProductNotAvailable(ProductNotAvailableException ex, Long userId) {
        throw ex;  // Re-throw to global handler
    }

    @Recover
    public OrderDto recoverFromInsufficientStock(InsufficientProductStockException ex, Long userId) {
        throw ex;  // Re-throw to global handler
    }

    // Generic fallback for lock-related exceptions after retry attempts and any other exceptions
    @Recover
    public OrderDto recoverCheckout(Throwable ex, Long userId) {
        throw new CheckoutFailException(userId, ex.getMessage());
    }
}