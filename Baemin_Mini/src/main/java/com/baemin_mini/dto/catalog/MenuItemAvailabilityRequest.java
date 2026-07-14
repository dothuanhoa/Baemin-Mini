package com.baemin_mini.dto.catalog;

import jakarta.validation.constraints.NotNull;

public record MenuItemAvailabilityRequest(
        @NotNull(message = "isAvailable is required")
        Boolean isAvailable
) {
}
