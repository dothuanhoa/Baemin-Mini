package com.baemin_mini.controller;

import com.baemin_mini.common.ApiResponse;
import com.baemin_mini.dto.admin.AdminRevenueResponse;
import com.baemin_mini.dto.admin.AdminStatsResponse;
import com.baemin_mini.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats/overview")
    public ApiResponse<AdminStatsResponse> getOverviewStats() {
        return ApiResponse.success("Overview stats fetched successfully", adminService.getOverviewStats());
    }

    @GetMapping("/revenue")
    public ApiResponse<AdminRevenueResponse> getRevenueStats() {
        return ApiResponse.success("Revenue stats fetched successfully", adminService.getRevenueStats());
    }
}
