package com.baemin_mini.service;

import com.baemin_mini.domain.enums.OrderStatus;
import com.baemin_mini.dto.order.OrderFeeRequest;
import com.baemin_mini.dto.order.OrderFeeResponse;
import com.baemin_mini.dto.order.OrderRequest;
import com.baemin_mini.dto.order.OrderResponse;
import com.baemin_mini.dto.order.OrderTrackingResponse;
import java.util.List;

public interface OrderService {
    OrderFeeResponse calculateFee(OrderFeeRequest request);

    OrderResponse createOrder(String username, OrderRequest request);

    List<OrderResponse> getMyOrders(String username);

    List<OrderResponse> getRestaurantOrders(String username, Long restaurantId);

    List<OrderTrackingResponse> getOrderTracking(String username, Long orderId);

    OrderResponse updateOrderStatus(String username, Long orderId, OrderStatus status);
}
