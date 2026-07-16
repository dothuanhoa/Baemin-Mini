package com.baemin_mini.service;

public interface DeliveryDispatchService {
    void dispatchOrderToNearbyShippers(Long orderId);

    void dispatchWaitingOrdersToShipper(String username);
}
