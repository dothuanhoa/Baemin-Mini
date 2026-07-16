package com.baemin_mini.dto.admin;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminRevenueResponse {
    private long totalDeliveredOrders;
    private BigDecimal totalPlatformFee;
}
