package com.baemin_mini.service.impl;

import com.baemin_mini.domain.enums.OrderStatus;
import com.baemin_mini.dto.admin.AdminRevenueResponse;
import com.baemin_mini.dto.admin.AdminStatsResponse;
import com.baemin_mini.repository.OrderRepository;
import com.baemin_mini.repository.RestaurantRepository;
import com.baemin_mini.repository.UserRepository;
import com.baemin_mini.service.AdminService;
import java.util.List;
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

        long waitingForShipper = orderRepository.countByShipperIdIsNullAndStatusIn(
                List.of(OrderStatus.PLACED, OrderStatus.PREPARING));
        long delivering = orderRepository.countByStatus(OrderStatus.DELIVERING);
        long delivered = orderRepository.countByStatus(OrderStatus.DELIVERED);

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalRestaurants(totalRestaurants)
                .totalOrders(totalOrders)
                .totalWaitingForShipper(waitingForShipper)
                .totalDeliveringOrders(delivering)
                .totalDeliveredOrders(delivered)
                .build();
    }

    @Override
    public AdminRevenueResponse getRevenueStats() {
        long delivered = orderRepository.countByStatus(OrderStatus.DELIVERED);
        return AdminRevenueResponse.builder()
                .totalDeliveredOrders(delivered)
                .totalPlatformFee(orderRepository.sumPlatformFeeByStatus(OrderStatus.DELIVERED))
                .build();
    }
}
