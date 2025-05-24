package com.example.eshop.model.dto.business.response;

import java.util.List;

public record UserOrderResponseDto(List<OrderResponseDto> orders) {
}
