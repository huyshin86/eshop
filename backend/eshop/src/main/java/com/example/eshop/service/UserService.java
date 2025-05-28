package com.example.eshop.service;

import com.example.eshop.exception.OrderNotFoundException;
import com.example.eshop.exception.UserNotFoundException;
import com.example.eshop.model.Order;
import com.example.eshop.model.OrderItem;
import com.example.eshop.model.Product;
import com.example.eshop.model.User;
import com.example.eshop.model.common.Role;
import com.example.eshop.model.dto.business.*;
import com.example.eshop.repository.interfaces.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserJpaRepository userRepo;

    @Transactional(readOnly = true)
    public UserInfoDto getUserInfo(Long userId) {
        User user = findUserById(userId);
        return mapToUserInfoResponseDto(user);
    }

    @Transactional(readOnly = true)
    public UserOrderDto getUserOrders(Long userId) {
        User user = findUserById(userId);
        return mapToUserOrderResponseDto(user);
    }

    public UserInfoDto updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        User user = findUserById(userId);

        updateUserFields(user, request);

        User savedUser = userRepo.save(user);
        return mapToUserInfoResponseDto(savedUser);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long userId, Long orderId) {
        User user = findUserById(userId);

        Order order = user.getOrders().stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new OrderNotFoundException(userId, orderId));

        return toOrderResponseDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByStatus(Long userId, String status) {
        User user = findUserById(userId);

        return user.getOrders().stream()
                .filter(order -> order.getOrderStatus().toString().equalsIgnoreCase(status))
                .map(this::toOrderResponseDto)
                .toList();
    }

    // Admin methods
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return findUserById(id);
    }

    @Transactional(readOnly = true)
    public List<User> searchUsersByEmail(String email) {
        return userRepo.findByEmailContainingIgnoreCase(email);
    }

    public User updateUserRole(Long id, String roleStr) {
        User user = findUserById(id);
        
        Role role;
        try {
            role = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleStr);
        }
        
        user.setRole(role);
        return userRepo.save(user);
    }

    public void deleteUser(Long id) {
        User user = findUserById(id);
        userRepo.delete(user);
    }

    @Transactional(readOnly = true)
    public Long getTotalUserCount() {
        return userRepo.count();
    }

    private void updateUserFields(User user, UpdateUserInfoRequest request) {
        if (request.firstName() != null && !request.firstName().trim().isEmpty()) {
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null && !request.lastName().trim().isEmpty()) {
            user.setLastName(request.lastName().trim());
        }
        if (request.phoneNumber() != null && !request.phoneNumber().trim().isEmpty()) {
            validatePhoneNumber(request.phoneNumber());
            user.setPhoneNumber(request.phoneNumber().trim());
        }
        if (request.address() != null && !request.address().trim().isEmpty()) {
            user.setAddress(request.address().trim());
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        // Business logic: Validate phone number format
        if (!phoneNumber.matches("^[+]?[0-9]{10,15}$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }

    // Helper methods
    private User findUserById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    // Mapping methods
    private UserInfoDto mapToUserInfoResponseDto(User user) {
        return new UserInfoDto(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getAddress()
        );
    }

    private UserOrderDto mapToUserOrderResponseDto(User user) {
        return new UserOrderDto(mapOrders(user.getOrders()));
    }

    private List<OrderDto> mapOrders(List<Order> orders) {
        return orders.stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
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
}
