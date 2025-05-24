package com.example.eshop.model.dto.business.response;

public record UserInfoResponseDto(
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String address) {
}
