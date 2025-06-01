package com.example.eshop.service;

import com.example.eshop.exception.*;
import com.example.eshop.model.*;
import com.example.eshop.model.dto.business.*;
import com.example.eshop.repository.interfaces.OrderJpaRepository;
import com.example.eshop.repository.interfaces.ProductJpaRepository;
import com.example.eshop.repository.interfaces.UserJpaRepository;
import com.example.eshop.util.LockTimeout;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final UserJpaRepository userRepo;
    private final ProductJpaRepository productRepo;
    private final OrderJpaRepository orderRepo;
    private final PayPalService payPalService;
    private final CartService cartService;

    private static final int calculationScale = 2;
    private static final RoundingMode calculationRoundingMode = RoundingMode.HALF_UP;

    @Transactional(noRollbackFor = Exception.class)
    public PaymentOrderDto initializeCheckout(Long userId) {
        Order businessOrder = createBusinessOrder(userId);

        // Save the order in PENDING state
        Order savedOrder = orderRepo.save(businessOrder);
        log.info("Business order created with ID: {} and number: {}",
                savedOrder.getOrderId(), savedOrder.getOrderNumber());

        try {
            // Create PayPal order
            PayPalOrderDetailDto paypalOrderDetail = payPalService.createPayPalOrder(savedOrder);

            // Store PayPal order ID in business order
            savedOrder.setPaypalOrderId(paypalOrderDetail.paypalOrderId());
            orderRepo.save(savedOrder);

            return new PaymentOrderDto(
                    savedOrder.getOrderId(),
                    savedOrder.getOrderNumber(),
                    paypalOrderDetail,
                    savedOrder.getGrandTotal(),
                    "PENDING"
            );

        } catch (PaymentProcessingException e) {
            log.error("Failed to create PayPal order for business order: {}",
                    savedOrder.getOrderNumber(), e);

            // Rollback inventory changes
            rollbackInventory(savedOrder);

            throw new CheckoutFailException(userId, "Payment processing failed: " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderDto completeCheckout(String paypalOrderId) {
        // Find business order by PayPal order ID
        Order businessOrder = orderRepo.findByPaypalOrderId(paypalOrderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for PayPal ID: " + paypalOrderId));

        if (businessOrder.getOrderStatus() == Order.OrderStatus.PROCESSING) {
            log.info("Order with PayPal ID: {} is already in {} state. Returning success.",
                    paypalOrderId, businessOrder.getOrderStatus());
            return toOrderResponseDto(businessOrder);
        }

        if (businessOrder.getOrderStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in pending state: " + businessOrder.getOrderNumber());
        }

        String status = payPalService.getOrderStatus(paypalOrderId);
        if (!"APPROVED".equalsIgnoreCase(status)) {
            throw new IllegalStateException("PayPal order is not approved for capture. Current status: " + status);
        }

        try {
            // Capture PayPal payment
            com.paypal.sdk.models.Order capturedPayPalOrder = payPalService.capturePayPalOrder(paypalOrderId);

            // Update business order status
            businessOrder.setOrderStatus(Order.OrderStatus.PROCESSING);
            businessOrder.setPaymentCapturedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));


            // Clear user's cart now that payment is successful
            clearCart(businessOrder.getUser());

            Order savedOrder = orderRepo.save(businessOrder);
            log.info("Order completed successfully: {}", savedOrder.getOrderNumber());

            return toOrderResponseDto(savedOrder);

        } catch (PaymentProcessingException e) {
            log.error("Failed to capture PayPal payment for order: {}",
                    businessOrder.getOrderNumber(), e);

            // Mark order as processing but don't roll back inventory yet
            businessOrder.setOrderStatus(Order.OrderStatus.PROCESSING);
            orderRepo.save(businessOrder);

            throw new PaymentCaptureException(businessOrder.getOrderNumber(), e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String paypalOrderId) {
        Order businessOrder = orderRepo.findByPaypalOrderId(paypalOrderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for PayPal ID: " + paypalOrderId));

        doCancelOrder(businessOrder);
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000) // 1 hour in ms
    public void cleanupStalePendingOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        List<Order> staleOrders = orderRepo.findByOrderStatusAndOrderDateBefore(Order.OrderStatus.PENDING, cutoff);

        for (Order order : staleOrders) {
            try {
                log.info("Cleaning up stale PENDING order: {}", order.getOrderNumber());
                processSingleStaleOrder(order);
            } catch (Exception e) {
                log.error("Failed to cancel stale order: {}", order.getOrderNumber(), e);
            }
        }
    }

    // Protected helper methods
    // Handles an individual stale order in its own transaction
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    protected void processSingleStaleOrder(Order order) {
        String paypalOrderId = order.getPaypalOrderId();
        LocalDateTime orderTime = order.getCreatedAt();
        boolean isExpired = orderTime.isBefore(LocalDateTime.now().minusHours(3));

        if (paypalOrderId == null || paypalOrderId.isBlank()) {
            log.warn("Stale PENDING order {} has no PayPal ID. Marking as CANCELLED.", order.getOrderNumber());
            doCancelOrder(order);
            return;
        }

        try {
            String payPalStatus = payPalService.getOrderStatus(paypalOrderId);

            if ("APPROVED".equalsIgnoreCase(payPalStatus)) {
                log.info("Order {} (PayPal ID: {}) is APPROVED on PayPal. Attempting capture.", order.getOrderNumber(), paypalOrderId);
                try {
                    payPalService.capturePayPalOrder(paypalOrderId);
                    order.setOrderStatus(Order.OrderStatus.PROCESSING);
                    order.setPaymentCapturedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
                    cartService.clearCart(order.getUser().getId());
                    orderRepo.save(order);
                    log.info("Successfully captured and processed order {}.", order.getOrderNumber());
                } catch (PaymentProcessingException e) {
                    // Capture failed even though PayPal said APPROVED. Payment might be captured manually later
                    order.setOrderStatus(Order.OrderStatus.MANUAL_REVIEW_PAYMENT);
                    orderRepo.save(order);
                    log.error("CRITICAL: Capture failed for APPROVED order {}. PayPal ID: {}. Error: {}",
                            order.getOrderNumber(), paypalOrderId, e.getMessage());
                }
            } else if (List.of("VOIDED", "COMPLETED", "DENIED").contains(payPalStatus.toUpperCase())) {
                // These are final statuses where no payment can happen
                log.info("Order {} (PayPal ID: {}) has final PayPal status {}. Cancelling locally.",
                        order.getOrderNumber(), paypalOrderId, payPalStatus);
                doCancelOrder(order);
            } else if (isExpired) {
                log.warn("Order {} is older than 3 hours with PayPal status {}. Marking as EXPIRED and cancelling.",
                        order.getOrderNumber(), payPalStatus);
                order.setOrderStatus(Order.OrderStatus.EXPIRED);
                rollbackInventory(order);
                orderRepo.save(order);
            } else {
                // PayPal status is CREATED, PENDING, or ambiguous no cancel yet
                log.warn("Order {} (PayPal ID: {}) has ambiguous PayPal status {}. Retaining for next cycle.",
                        order.getOrderNumber(), paypalOrderId, payPalStatus);
            }
        } catch (PaymentProcessingException e) {
            if (isExpired) {
                log.warn("Order {} is older than 3 hours and PayPal status could not be retrieved. Marking as EXPIRED.",
                        order.getOrderNumber());
                order.setOrderStatus(Order.OrderStatus.EXPIRED);
                rollbackInventory(order);
                orderRepo.save(order);
            } else {
                log.error("Failed to retrieve PayPal status for order {}: {}. Will retry next cycle.",
                        order.getOrderNumber(), e.getMessage());
            }
        }
    }

    // Private helper methods
    private void doCancelOrder(Order businessOrder) {
        if (businessOrder.getOrderStatus() == Order.OrderStatus.PENDING) {
            rollbackInventory(businessOrder);
            businessOrder.setOrderStatus(Order.OrderStatus.CANCELLED);
            businessOrder.setPaypalOrderId(null);
            orderRepo.save(businessOrder);
            log.info("Order cancelled: {}", businessOrder.getOrderNumber());
        } else {
            log.info("Cancel order called but order {} is in status {}, skipping cancel",
                    businessOrder.getOrderNumber(), businessOrder.getOrderStatus());
        }
    }

    @Retryable(
            retryFor = {
                    PessimisticLockException.class,
                    CannotAcquireLockException.class,
                    LockAcquisitionException.class
            },
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    @LockTimeout() // Resource locking for race conditions
    @Transactional(timeout = 15, rollbackFor = Exception.class)
    private Order createBusinessOrder(Long userId) {
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
        List<Product> products = productRepo.findAllByIdForUpdate(productIds);

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

            // Reserve stock (decrease it)
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

        BigDecimal shipping = BigDecimal.valueOf(20);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1));
        BigDecimal grandTotal = subtotal.add(shipping).add(tax);

        subtotal = subtotal.setScale(calculationScale, calculationRoundingMode);
        tax = tax.setScale(calculationScale, calculationRoundingMode);
        grandTotal = grandTotal.setScale(calculationScale, calculationRoundingMode);

        order.setSubtotal(subtotal);
        order.setShippingCost(shipping);
        order.setTax(tax);
        order.setGrandTotal(grandTotal);

        return order;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void rollbackInventory(Order order) {
        try {
            List<Product> rollbackProducts = order.getOrderItems().stream()
                    .map(item -> {
                        Product product = item.getProduct();
                        product.setStock(product.getStock() + item.getQuantity());
                        return product;
                    }).toList();
            productRepo.saveAll(rollbackProducts);
            log.info("Inventory rollback completed for order: {}", order.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to rollback inventory for order: {}", order.getOrderNumber(), e);
            log.warn("ALERT: Inventory rollback failed. Manual review required for order: {}", order.getOrderNumber());

        }
    }

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

    // Catch business exceptions
    @Recover
    public OrderDto recoverFromUserNotFound(UserNotFoundException ex, Long userId) {
        throw ex;  // Re-throw to global handler since it has its own response
    }

    @Recover
    public OrderDto recoverFromCartEmpty(CartEmptyException ex, Long userId) {
        throw ex;  // Re-throw to global handler since it has its own response
    }

    @Recover
    public OrderDto recoverFromShippingMissing(ShippingAddressMissingException ex, Long userId) {
        throw ex;  // Re-throw to global handler since it has its own response
    }

    @Recover
    public OrderDto recoverFromProductNotFound(ProductNotFoundException ex, Long userId) {
        throw ex;  // Re-throw to global handler since it has its own response
    }

    @Recover
    public OrderDto recoverFromProductNotAvailable(ProductNotAvailableException ex, Long userId) {
        throw ex;  // Re-throw to global handler since it has its own response
    }

    @Recover
    public OrderDto recoverFromInsufficientStock(InsufficientProductStockException ex, Long userId) {
        throw ex;  // Re-throw to global handler since it has its own response
    }

    // Generic fallback for lock-related exceptions after retry attempts and any other exceptions
    @Recover
    public OrderDto recoverCheckout(Throwable ex, Long userId) {
        throw new CheckoutFailException(userId, ex.getMessage());
    }
}