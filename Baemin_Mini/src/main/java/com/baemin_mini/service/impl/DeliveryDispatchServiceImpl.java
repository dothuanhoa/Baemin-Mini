package com.baemin_mini.service.impl;

import com.baemin_mini.domain.entity.ShipperProfile;
import com.baemin_mini.service.DeliveryAssignmentService;
import com.baemin_mini.service.DeliveryDispatchService;
import com.baemin_mini.service.SseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryDispatchServiceImpl implements DeliveryDispatchService {

    private final DeliveryAssignmentService deliveryAssignmentService;
    private final SseService sseService;

    @Override
    public void dispatchOrderToNearbyShippers(Long orderId) {
        List<ShipperProfile> availableShippers = deliveryAssignmentService.findAvailableShippersForOrder(orderId);
        if (availableShippers.isEmpty()) {
            log.info("No available shippers found to dispatch order {}", orderId);
            return;
        }

        for (ShipperProfile shipper : availableShippers) {
            sseService.notifyShipper(
                    shipper.getUser().getId(),
                    "NEW_ORDER_DISPATCH",
                    "New order available: " + orderId);
        }

        log.info("Dispatched order {} to {} nearby shippers", orderId, availableShippers.size());
    }
}
