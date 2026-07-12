package com.baemin_mini.dto.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserAddressResponse(
        Long id,
        String title,
        String receiverName,
        String receiverPhone,
        String addressLine,
        BigDecimal latitude,
        BigDecimal longitude,
        Boolean isDefault,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}