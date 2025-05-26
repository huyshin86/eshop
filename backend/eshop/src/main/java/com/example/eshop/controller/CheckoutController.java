package com.example.eshop.controller;

import com.example.eshop.model.dto.business.OrderDto;
import com.example.eshop.model.dto.common.SuccessResponse;
import com.example.eshop.security.util.SecurityUtils;
import com.example.eshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> checkout() {
        Long userId = SecurityUtils.getCurrentUserId();
        OrderDto orderDto = orderService.checkout(userId);

        return ResponseEntity.ok(
                new SuccessResponse<>(orderDto)
        );
    }
}
