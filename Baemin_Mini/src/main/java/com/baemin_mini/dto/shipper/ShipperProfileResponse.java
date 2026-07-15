package com.baemin_mini.dto.shipper;

import com.baemin_mini.domain.enums.ShipperStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShipperProfileResponse(
        Long id,
        ShipperStatus currentStatus,
        BigDecimal currentLatitude,
        BigDecimal currentLongitude,
        LocalDateTime lastLocationAt) {
}