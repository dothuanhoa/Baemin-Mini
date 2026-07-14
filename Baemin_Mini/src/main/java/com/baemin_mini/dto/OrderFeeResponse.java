package com.baemin_mini.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderFeeResponse {
    private BigDecimal distanceKm;
    private BigDecimal deliveryFee;
}
