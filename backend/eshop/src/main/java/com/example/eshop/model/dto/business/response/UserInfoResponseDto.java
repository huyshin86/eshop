package com.example.eshop.model.dto.business.response;

import java.util.List;

public record UserInfoResponseDto(
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String address) {
}
