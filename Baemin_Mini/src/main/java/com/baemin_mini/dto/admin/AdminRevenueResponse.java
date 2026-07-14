package com.baemin_mini.dto.admin;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRevenueResponse {
    private BigDecimal totalPlatformFee;
    private long totalDeliveredOrders;
}
