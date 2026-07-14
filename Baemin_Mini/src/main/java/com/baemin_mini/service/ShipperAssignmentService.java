package com.baemin_mini.service;

import com.baemin_mini.domain.entity.Order;

public interface ShipperAssignmentService {
    void assignNearestShipper(Long orderId);
}
