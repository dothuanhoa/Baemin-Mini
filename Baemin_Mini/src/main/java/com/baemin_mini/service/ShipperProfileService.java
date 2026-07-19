package com.baemin_mini.service;

import com.baemin_mini.domain.enums.ShipperStatus;
import com.baemin_mini.dto.order.OrderResponse;
import com.baemin_mini.dto.shipper.ShipperLocationRequest;
import com.baemin_mini.dto.shipper.ShipperProfileResponse;
import java.util.List;

public interface ShipperProfileService {
    ShipperProfileResponse getProfile(String username);

    ShipperProfileResponse updateLocation(String username, ShipperLocationRequest request);

    ShipperProfileResponse updateStatus(String username, ShipperStatus status);


    List<OrderResponse> getMyActiveOrders(String username);

    List<OrderResponse> getMyOrderHistory(String username);

    OrderResponse startDelivery(String username, Long orderId);

    OrderResponse completeDelivery(String username, Long orderId);
}
