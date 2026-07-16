package com.baemin_mini.service;

import com.baemin_mini.dto.admin.AdminRevenueResponse;
import com.baemin_mini.dto.admin.AdminStatsResponse;

public interface AdminService {
    AdminStatsResponse getOverviewStats();
    AdminRevenueResponse getRevenueStats();
}
