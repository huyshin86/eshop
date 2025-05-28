package com.example.eshop.controller;

import com.example.eshop.model.dto.business.PayPalOrderDetailDto;
import com.example.eshop.model.dto.business.PaymentOrderDto;
import com.example.eshop.security.SecurityConfig;
import com.example.eshop.service.OrderService;
import com.example.eshop.security.util.CurrentUserProvider;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Disabled("Temporarily disabled due to ongoing debugging of context setup")
@WebMvcTest(
        controllers = CheckoutController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class}
)
@Import({SecurityConfig.class, CheckoutControllerTest.TestConfig.class}) // Import the test-specific configuration
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Autowire the mocks that are defined in TestConfig
    @Autowired
    private OrderService orderService;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    // Define a static nested @Configuration class mocks
    @Configuration
    static class TestConfig {
        @Bean
        public OrderService orderService() {
            return mock(OrderService.class);
        }

        @Bean
        public CurrentUserProvider currentUserProvider() {
            return mock(CurrentUserProvider.class);
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return mock(UserDetailsService.class);
        }
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "CUSTOMER")
    void initializeCheckout_shouldReturnPaymentOrder() throws Exception {
        PayPalOrderDetailDto detail = new PayPalOrderDetailDto("PAYPAL123", "Link");

        PaymentOrderDto mockPayment = new PaymentOrderDto(1L, "ORD123", detail, null, "PENDING");

        // Mock the CurrentUserProvider to return a specific user ID
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        // Mock the behavior of orderService.initializeCheckout when called with 1L
        when(orderService.initializeCheckout(1L)).thenReturn(mockPayment);

        mockMvc.perform(post("/api/checkout/initialize")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user("user@example.com").password("password").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paypalOrderId").value("PAYPAL123"))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD123"));
    }
}