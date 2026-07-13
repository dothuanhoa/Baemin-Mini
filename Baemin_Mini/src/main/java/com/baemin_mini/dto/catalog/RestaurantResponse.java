package com.baemin_mini.dto.catalog;

import java.math.BigDecimal;

public record RestaurantResponse(
        Long id,
        String name,
        String address,
        String phoneContact,
        BigDecimal latitude,
        BigDecimal longitude,
        Boolean isOpen,
        AreaResponse area
) {
}
