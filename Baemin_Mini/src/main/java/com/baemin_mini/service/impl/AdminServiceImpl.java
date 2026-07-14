package com.baemin_mini.service.impl;

import com.baemin_mini.domain.enums.OrderStatus;
import com.baemin_mini.dto.admin.AdminRevenueResponse;
import com.baemin_mini.dto.admin.AdminStatsResponse;
import com.baemin_mini.repository.OrderRepository;
import com.baemin_mini.repository.RestaurantRepository;
import com.baemin_mini.repository.UserRepository;
import com.baemin_mini.service.AdminService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    @Override
    public AdminStatsResponse getOverviewStats() {
        long totalUsers = userRepository.count();
        long totalRestaurants = restaurantRepository.count();
        long totalOrders = orderRepository.count();
        // Assuming no strict daily new users calculation for MVP, or just 0 for now.
        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalRestaurants(totalRestaurants)
                .totalOrders(totalOrders)
                .newUsersToday(0)
                .build();
    }

    @Override
    public AdminRevenueResponse getRevenueStats() {
        long totalDeliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        BigDecimal totalPlatformFee = orderRepository.sumPlatformFeeForDelivered();
        
        return AdminRevenueResponse.builder()
                .totalDeliveredOrders(totalDeliveredOrders)
                .totalPlatformFee(totalPlatformFee)
                .build();
    }
}
