package com.baemin_mini.dto.catalog;

import java.math.BigDecimal;

public record MenuItemResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        Boolean isAvailable,
        CategoryResponse category
) {
}
