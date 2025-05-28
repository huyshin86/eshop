package com.example.eshop.controller;

import com.example.eshop.model.dto.business.OrderDto;
import com.example.eshop.model.dto.business.PaymentOrderDto;
import com.example.eshop.model.dto.common.SuccessResponse;
import com.example.eshop.security.util.CurrentUserProvider;
//import com.example.eshop.security.util.SecurityUtils;
import com.example.eshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final OrderService orderService;
    private final CurrentUserProvider currentUserProvider;

    // Returns PayPal order ID for frontend to redirect user to PayPal
    @PostMapping("/initialize")
    public ResponseEntity<?> initializeCheckout() {
        Long userId = currentUserProvider.getCurrentUserId();
        PaymentOrderDto paymentOrder = orderService.initializeCheckout(userId);

        log.info("Checkout initialized for user: {}, PayPal Order ID: {}",
                userId, paymentOrder.payPalOrderDetailDto().paypalOrderId());

        return ResponseEntity.ok(
                new SuccessResponse<>(paymentOrder)
        );
    }

    // Called when user returns from PayPal after approving payment
    @PostMapping("/complete")
    public ResponseEntity<?> completeCheckout(@RequestBody Map<String, String> request) {
        String paypalOrderId = request.get("paypalOrderId");
        OrderDto completedOrder = orderService.completeCheckout(paypalOrderId);

        log.info("Checkout completed for PayPal Order ID: {}", paypalOrderId);

        return ResponseEntity.ok(
                new SuccessResponse<>(completedOrder)
        );
    }

    // Cancel checkout (for handling user abandonment or payment failure)
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelCheckout(@RequestBody Map<String, String> request) {
        String paypalOrderId = request.get("paypalOrderId");
        orderService.cancelOrder(paypalOrderId);

        log.info("Checkout cancelled for PayPal Order ID: {}", paypalOrderId);

        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK, "Checkout cancelled successfully")
        );
    }
}
