package com.baemin_mini.dto.shipper;

import com.baemin_mini.dto.order.OrderResponse;
import java.math.BigDecimal;

public record NearbyOrderResponse(
        OrderResponse order,
        BigDecimal distanceKm) {
}