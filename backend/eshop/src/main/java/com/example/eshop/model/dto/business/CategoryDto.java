package com.example.eshop.model.dto.business;

import java.time.LocalDateTime;

public record CategoryDto(
        Long id,
        String name,
        String slug,
        String description,
        String categoryIconUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
