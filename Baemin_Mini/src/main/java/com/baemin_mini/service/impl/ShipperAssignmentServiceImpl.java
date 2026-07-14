package com.baemin_mini.service.impl;

import com.baemin_mini.domain.entity.Order;
import com.baemin_mini.domain.entity.OrderTracking;
import com.baemin_mini.domain.entity.Restaurant;
import com.baemin_mini.domain.entity.ShipperProfile;
import com.baemin_mini.domain.enums.OrderStatus;
import com.baemin_mini.domain.enums.ShipperStatus;
import com.baemin_mini.repository.OrderRepository;
import com.baemin_mini.repository.RestaurantRepository;
import com.baemin_mini.repository.ShipperProfileRepository;
import com.baemin_mini.service.FeeService;
import com.baemin_mini.service.ShipperAssignmentService;
import com.baemin_mini.service.SseService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipperAssignmentServiceImpl implements ShipperAssignmentService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final ShipperProfileRepository shipperProfileRepository;
    private final FeeService feeService;
    private final SseService sseService;

    @Override
    @Transactional
    public void assignNearestShipper(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            log.warn("Order {} is not ready for pickup. Current status: {}", orderId, order.getStatus());
            return;
        }

        Restaurant restaurant = restaurantRepository.findById(order.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        // Get shippers active in last 30 minutes
        LocalDateTime thirtyMinsAgo = LocalDateTime.now().minusMinutes(30);
        List<ShipperProfile> availableShippers = shipperProfileRepository
                .findByCurrentStatusAndCurrentLatitudeIsNotNullAndCurrentLongitudeIsNotNullAndLastLocationAtAfter(
                        ShipperStatus.AVAILABLE, thirtyMinsAgo);

        if (availableShippers.isEmpty()) {
            log.info("No available shippers found for order {}", orderId);
            return;
        }

        ShipperProfile nearestShipper = null;
        BigDecimal shortestDistance = null;

        for (ShipperProfile shipper : availableShippers) {
            BigDecimal distance = feeService.calculateDistanceKm(
                    shipper.getCurrentLatitude(), shipper.getCurrentLongitude(),
                    restaurant.getLatitude(), restaurant.getLongitude()
            );

            if (shortestDistance == null || distance.compareTo(shortestDistance) < 0) {
                shortestDistance = distance;
                nearestShipper = shipper;
            }
        }

        if (nearestShipper != null) {
            log.info("Assigned shipper {} to order {} (distance: {} km)", nearestShipper.getId(), orderId, shortestDistance);
            
            // Assign order
            order.setShipperId(nearestShipper.getUser().getId());
            order.setStatus(OrderStatus.ASSIGNED);
            
            // Create tracking
            OrderTracking tracking = OrderTracking.builder()
                    .order(order)
                    .status(OrderStatus.ASSIGNED)
                    .note("System assigned nearest shipper")
                    .actorRole("SYSTEM")
                    .actorId(0L)
                    .build();
            order.addTracking(tracking);
            
            // Update shipper status
            nearestShipper.setCurrentStatus(ShipperStatus.BUSY);
            
            orderRepository.save(order);
            shipperProfileRepository.save(nearestShipper);
            
            // SSE call to notify shipper
            sseService.notifyShipper(nearestShipper.getUser().getId(), "NEW_ORDER_ASSIGNED", "You have been assigned to order ID: " + orderId);
        }
    }
}
