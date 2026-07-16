package com.baemin_mini.service.impl;

import com.baemin_mini.domain.entity.Order;
import com.baemin_mini.domain.entity.Restaurant;
import com.baemin_mini.domain.entity.ShipperProfile;
import com.baemin_mini.domain.enums.ShipperStatus;
import com.baemin_mini.repository.OrderRepository;
import com.baemin_mini.repository.RestaurantRepository;
import com.baemin_mini.repository.ShipperProfileRepository;
import com.baemin_mini.service.DeliveryDispatchService;
import com.baemin_mini.service.FeeService;
import com.baemin_mini.service.SseService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryDispatchServiceImpl implements DeliveryDispatchService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final ShipperProfileRepository shipperProfileRepository;
    private final FeeService feeService;
    private final SseService sseService;

    private static final BigDecimal MAX_RADIUS_KM = BigDecimal.valueOf(5.0);

    @Override
    public void dispatchOrderToNearbyShippers(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        Restaurant restaurant = restaurantRepository.findById(order.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        // Get shippers active in last 30 minutes
        LocalDateTime thirtyMinsAgo = LocalDateTime.now().minusMinutes(30);
        List<ShipperProfile> availableShippers = shipperProfileRepository
                .findByCurrentStatusAndCurrentLatitudeIsNotNullAndCurrentLongitudeIsNotNullAndLastLocationAtAfter(
                        ShipperStatus.AVAILABLE, thirtyMinsAgo);

        if (availableShippers.isEmpty()) {
            log.info("No available shippers found to dispatch order {}", orderId);
            return;
        }

        int count = 0;
        for (ShipperProfile shipper : availableShippers) {
            BigDecimal distance = feeService.calculateDistanceKm(
                    shipper.getCurrentLatitude(), shipper.getCurrentLongitude(),
                    restaurant.getLatitude(), restaurant.getLongitude()
            );

            if (distance.compareTo(MAX_RADIUS_KM) <= 0) {
                // Broadcast SSE to this shipper
                sseService.notifyShipper(shipper.getUser().getId(), "NEW_ORDER_DISPATCH", "New order available: " + orderId);
                count++;
            }
        }
        
        log.info("Dispatched order {} to {} shippers within {} km", orderId, count, MAX_RADIUS_KM);
    }
}
