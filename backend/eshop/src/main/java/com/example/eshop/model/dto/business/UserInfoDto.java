package com.example.eshop.model.dto.business;

public record UserInfoDto(
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String address
) {
}
