package com.baemin_mini.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsResponse {
    private long totalUsers;
    private long totalRestaurants;
    private long totalOrders;
    private long totalWaitingForShipper;
    private long totalDeliveringOrders;
    private long totalDeliveredOrders;
}
