package com.baemin_mini.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long totalRestaurants;
    private long totalOrders;
    private long newUsersToday; // Optional, might just return 0 for MVP if complex
}
