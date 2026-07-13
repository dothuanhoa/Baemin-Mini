package com.baemin_mini.dto.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MenuItemRequest(
        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotBlank(message = "Name is required")
        String name,

        String description,

        @NotNull(message = "Price is required")
        @Min(value = 0, message = "Price must be non-negative")
        BigDecimal price,

        String imageUrl,

        Boolean isAvailable
) {
}
