package com.example.eshop.model.dto.business;

import java.util.List;

public record UserOrderDto(List<OrderDto> orders) {
}
