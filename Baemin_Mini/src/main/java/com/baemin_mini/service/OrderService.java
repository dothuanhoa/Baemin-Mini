package com.baemin_mini.service;

import com.baemin_mini.dto.OrderFeeRequest;
import com.baemin_mini.dto.OrderFeeResponse;
import com.baemin_mini.dto.OrderRequest;
import com.baemin_mini.dto.OrderResponse;
import com.baemin_mini.dto.OrderTrackingResponse;
import java.util.List;

public interface OrderService {
    OrderFeeResponse calculateFee(OrderFeeRequest request);
    OrderResponse createOrder(Long customerId, OrderRequest request);
    List<OrderResponse> getMyOrders(Long customerId);
    List<OrderResponse> getRestaurantOrders(Long restaurantId);
    List<OrderTrackingResponse> getOrderTracking(Long orderId);
    OrderResponse updateOrderStatus(Long orderId, String statusStr, Long actorId, String actorRole);
}
