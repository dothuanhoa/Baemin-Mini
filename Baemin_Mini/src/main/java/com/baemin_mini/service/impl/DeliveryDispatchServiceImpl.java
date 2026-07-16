package com.baemin_mini.service.impl;

import com.baemin_mini.common.exception.NotFoundException;
import com.baemin_mini.domain.entity.ShipperProfile;
import com.baemin_mini.domain.entity.User;
import com.baemin_mini.domain.event.OrderWaitingForShipperEvent;
import com.baemin_mini.dto.shipper.NearbyOrderResponse;
import com.baemin_mini.repository.UserRepository;
import com.baemin_mini.service.DeliveryAssignmentService;
import com.baemin_mini.service.DeliveryDispatchService;
import com.baemin_mini.service.SseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryDispatchServiceImpl implements DeliveryDispatchService {

    private final DeliveryAssignmentService deliveryAssignmentService;
    private final SseService sseService;
    private final UserRepository userRepository;

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

    @Override
    public void dispatchWaitingOrdersToShipper(String username) {
        User shipper = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<NearbyOrderResponse> nearbyOrders = deliveryAssignmentService.getNearbyOrders(username);
        if (nearbyOrders.isEmpty()) {
            log.info("No waiting orders found to dispatch to shipper {}", shipper.getId());
            return;
        }

        for (NearbyOrderResponse nearbyOrder : nearbyOrders) {
            Long orderId = nearbyOrder.order().getId();
            sseService.notifyShipper(
                    shipper.getId(),
                    "NEW_ORDER_DISPATCH",
                    "New order available: " + orderId);
        }

        log.info("Dispatched {} waiting orders to shipper {}", nearbyOrders.size(), shipper.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderWaitingForShipper(OrderWaitingForShipperEvent event) {
        dispatchOrderToNearbyShippers(event.orderId());
    }
}
