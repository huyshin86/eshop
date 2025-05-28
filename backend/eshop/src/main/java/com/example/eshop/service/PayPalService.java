package com.example.eshop.service;

import com.example.eshop.model.dto.business.PayPalOrderDetailDto;
import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.controllers.OrdersController;
import com.paypal.sdk.exceptions.ApiException;
import com.paypal.sdk.http.response.ApiResponse;
import com.paypal.sdk.models.*;
import com.example.eshop.model.Order;
import com.example.eshop.model.OrderItem;
import com.example.eshop.exception.PaymentProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayPalService {

    private final PaypalServerSdkClient paypalClient;

    private static final int PAYPAL_DECIMAL_SCALE = 2;
    private static final RoundingMode PAYPAL_ROUNDING_MODE = RoundingMode.HALF_UP;

    // Creates a PayPal order based on business order data
    public PayPalOrderDetailDto createPayPalOrder(Order businessOrder) throws PaymentProcessingException {
        try {
            CreateOrderInput createOrderInput = buildCreateOrderInput(businessOrder);
            OrdersController ordersController = paypalClient.getOrdersController();
            ApiResponse<com.paypal.sdk.models.Order> apiResponse = ordersController.createOrder(createOrderInput);

            com.paypal.sdk.models.Order paypalOrder = apiResponse.getResult();
            log.info("PayPal order created with ID: {}", paypalOrder.getId());

            String approvalUrl = null;
            if (paypalOrder.getLinks() != null) {
                approvalUrl = paypalOrder.getLinks().stream()
                        .filter(link -> "approve".equals(link.getRel()))
                        .findFirst()
                        .map(LinkDescription::getHref)
                        .orElse(null);
            }

            if (approvalUrl == null) {
                log.warn("No approval URL found for PayPal order: {}", paypalOrder.getId());
                throw new PaymentProcessingException("Failed to get approval URL from PayPal order");
            }

            return new PayPalOrderDetailDto(paypalOrder.getId(), approvalUrl);

        } catch (ApiException | IOException e) {
            log.error("Failed to create PayPal order for business order: {}", businessOrder.getOrderNumber(), e);
            throw new PaymentProcessingException("Failed to create PayPal order: " + e.getMessage());
        }
    }

    // Captures a PayPal order
    public com.paypal.sdk.models.Order capturePayPalOrder(String paypalOrderId) throws PaymentProcessingException {
        try {
            CaptureOrderInput ordersCaptureInput = new CaptureOrderInput.Builder(paypalOrderId, null).build();
            OrdersController ordersController = paypalClient.getOrdersController();
            ApiResponse<com.paypal.sdk.models.Order> apiResponse = ordersController.captureOrder(ordersCaptureInput);

            com.paypal.sdk.models.Order capturedOrder = apiResponse.getResult();
            log.info("PayPal order captured: {}", paypalOrderId);

            return capturedOrder;

        } catch (ApiException | IOException e) {
            log.error("Failed to capture PayPal order: {}", paypalOrderId, e);
            throw new PaymentProcessingException("Failed to capture PayPal order: " + e.getMessage());
        }
    }

    // Check PayPal order status for capture
    public String getOrderStatus(String paypalOrderId) throws PaymentProcessingException {
        try {
            OrdersController ordersController = paypalClient.getOrdersController();
            GetOrderInput getOrderInput = new GetOrderInput.Builder(paypalOrderId).build();

            ApiResponse<com.paypal.sdk.models.Order> apiResponse = ordersController.getOrder(getOrderInput);
            com.paypal.sdk.models.Order paypalOrder = apiResponse.getResult();

            if (paypalOrder == null) {
                throw new PaymentProcessingException("PayPal order not found: " + paypalOrderId);
            }

            String status = paypalOrder.getStatus().toString();  // status like "CREATED", "APPROVED", "COMPLETED"
            log.info("PayPal order ID {} status: {}", paypalOrderId, status);

            return status;

        } catch (ApiException | IOException e) {
            log.error("Failed to get PayPal order status for ID: {}", paypalOrderId, e);
            throw new PaymentProcessingException("Unable to retrieve PayPal order status: " + e.getMessage());
        }
    }

    // Builds PayPal CreateOrderInput from business order
    private CreateOrderInput buildCreateOrderInput(Order businessOrder) {
        // Convert order items to PayPal items
        List<Item> paypalItems = businessOrder.getOrderItems().stream()
                .map(this::convertToPayPalItem)
                .collect(Collectors.toList());

        BigDecimal subtotalRounded = businessOrder.getSubtotal().setScale(PAYPAL_DECIMAL_SCALE, PAYPAL_ROUNDING_MODE);
        BigDecimal shippingCostRounded = businessOrder.getShippingCost().setScale(PAYPAL_DECIMAL_SCALE, PAYPAL_ROUNDING_MODE);
        BigDecimal taxRounded = businessOrder.getTax().setScale(PAYPAL_DECIMAL_SCALE, PAYPAL_ROUNDING_MODE);
        BigDecimal grandTotalRounded = businessOrder.getGrandTotal().setScale(PAYPAL_DECIMAL_SCALE, PAYPAL_ROUNDING_MODE);

        // Build amount breakdown
        AmountBreakdown breakdown = new AmountBreakdown.Builder()
                .itemTotal(new Money("USD", subtotalRounded.toPlainString()))
                .shipping(new Money("USD", shippingCostRounded.toPlainString()))
                .taxTotal(new Money("USD", taxRounded.toPlainString()))
                .build();

        BigDecimal calculatedBreakdownSum = subtotalRounded.add(shippingCostRounded).add(taxRounded);
        if (grandTotalRounded.compareTo(calculatedBreakdownSum) != 0) {
            log.warn("PayPal amount mismatch detected! Grand Total: {}, Breakdown Sum: {}. This might cause a 422 error.", grandTotalRounded.toPlainString(), calculatedBreakdownSum.toPlainString());

            grandTotalRounded = calculatedBreakdownSum;
        }

        // Build main amount
        AmountWithBreakdown amount = new AmountWithBreakdown.Builder(
                "USD",
                grandTotalRounded.toPlainString()
        ).breakdown(breakdown).build();

        // Build purchase unit
        PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest.Builder(amount)
                .referenceId(businessOrder.getOrderNumber())
                .description("Order #" + businessOrder.getOrderNumber() + " from E-Shop")
                .items(paypalItems)
                .shipping(buildShippingDetail(businessOrder))
                .build();

        // Build order request
        OrderRequest orderRequest = new OrderRequest.Builder(
                CheckoutPaymentIntent.CAPTURE,
                Collections.singletonList(purchaseUnit)
        ).build();

        return new CreateOrderInput.Builder(null, orderRequest).build();
    }

    // Converts business order item to PayPal item
    private Item convertToPayPalItem(OrderItem orderItem) {
        BigDecimal unitPriceRounded = orderItem.getUnitPrice().setScale(PAYPAL_DECIMAL_SCALE, PAYPAL_ROUNDING_MODE);

        return new Item.Builder(
                orderItem.getProduct().getProductName(),
                new Money.Builder("USD", unitPriceRounded.toPlainString()).build(),
                String.valueOf(orderItem.getQuantity())
        )
                .description(orderItem.getProduct().getDescription())
                .sku(orderItem.getProduct().getProductName().replaceAll(" ", "-"))
                .category(ItemCategory.PHYSICAL_GOODS)
                .build();
    }

    // Builds shipping details for PayPal
    private ShippingDetails buildShippingDetail(Order businessOrder) {
        ShippingName name = new ShippingName.Builder()
                .fullName(businessOrder.getUser().getFirstName() + " " + businessOrder.getUser().getLastName())
                .build();
        Address address = new Address.Builder()
                .addressLine1(businessOrder.getShippingAddress())
                .adminArea2("N/A")
                .adminArea1("N/A")
                .postalCode("00700")
                .countryCode("VN")
                .build();

        return new ShippingDetails.Builder()
                .name(name)
                .address(address)
                .build();
    }
}