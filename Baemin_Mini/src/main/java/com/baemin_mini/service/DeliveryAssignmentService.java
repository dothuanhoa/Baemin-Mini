package com.baemin_mini.service;

import com.baemin_mini.domain.entity.ShipperProfile;
import com.baemin_mini.dto.order.OrderResponse;
import com.baemin_mini.dto.shipper.NearbyOrderResponse;
import com.baemin_mini.dto.shipper.ShipperCancelOrderRequest;
import java.util.List;

public interface DeliveryAssignmentService {
    List<NearbyOrderResponse> getNearbyOrders(String username);

    OrderResponse acceptOrder(String username, Long orderId);

    OrderResponse cancelOrder(String username, Long orderId, ShipperCancelOrderRequest request);

    List<ShipperProfile> findAvailableShippersForOrder(Long orderId);
}