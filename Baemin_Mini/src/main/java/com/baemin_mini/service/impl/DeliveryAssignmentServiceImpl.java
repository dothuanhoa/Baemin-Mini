package com.baemin_mini.service.impl;

import com.baemin_mini.common.exception.BadRequestException;
import com.baemin_mini.common.exception.NotFoundException;
import com.baemin_mini.domain.entity.DeliveryAssignment;
import com.baemin_mini.domain.entity.Order;
import com.baemin_mini.domain.entity.OrderItem;
import com.baemin_mini.domain.entity.Restaurant;
import com.baemin_mini.domain.entity.ShipperProfile;
import com.baemin_mini.domain.entity.User;
import com.baemin_mini.domain.enums.DeliveryAssignmentStatus;
import com.baemin_mini.domain.enums.OrderStatus;
import com.baemin_mini.domain.enums.ShipperStatus;
import com.baemin_mini.domain.event.OrderWaitingForShipperEvent;
import com.baemin_mini.dto.order.OrderItemResponse;
import com.baemin_mini.dto.order.OrderResponse;
import com.baemin_mini.dto.shipper.NearbyOrderResponse;
import com.baemin_mini.dto.shipper.ShipperCancelOrderRequest;
import com.baemin_mini.repository.DeliveryAssignmentRepository;
import com.baemin_mini.repository.OrderRepository;
import com.baemin_mini.repository.RestaurantRepository;
import com.baemin_mini.repository.ShipperProfileRepository;
import com.baemin_mini.repository.UserRepository;
import com.baemin_mini.service.DeliveryAssignmentService;
import com.baemin_mini.service.FeeService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryAssignmentServiceImpl implements DeliveryAssignmentService {
    private static final BigDecimal MAX_PICKUP_DISTANCE_KM = BigDecimal.valueOf(5);
    private static final int ACTIVE_LOCATION_MINUTES = 30;
    private static final List<OrderStatus> WAITING_FOR_SHIPPER_STATUSES = List.of(
            OrderStatus.PLACED,
            OrderStatus.PREPARING);
    private static final List<OrderStatus> SHIPPER_ACTIVE_ORDER_STATUSES = List.of(
            OrderStatus.PLACED,
            OrderStatus.PREPARING,
            OrderStatus.DELIVERING);
    private static final List<DeliveryAssignmentStatus> HIDDEN_AFTER_CANCEL_STATUSES = List.of(
            DeliveryAssignmentStatus.CANCELLED);

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final ShipperProfileRepository shipperProfileRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final FeeService feeService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public List<NearbyOrderResponse> getNearbyOrders(String username) {
        User shipper = getUser(username);
        ShipperProfile profile = getRequiredReadyProfile(shipper);

        return orderRepository.findByStatusInAndShipperIdIsNullOrderByCreatedAtAsc(WAITING_FOR_SHIPPER_STATUSES)
                .stream()
                .filter(order -> !hasCancelledThisOrder(order.getId(), shipper.getId()))
                .map(order -> toNearbyOrder(order, profile))
                .filter(response -> response.distanceKm().compareTo(MAX_PICKUP_DISTANCE_KM) <= 0)
                .sorted(Comparator.comparing(NearbyOrderResponse::distanceKm))
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse acceptOrder(String username, Long orderId) {
        User shipper = getUser(username);
        ShipperProfile profile = getRequiredReadyProfile(shipper);
        ensureNoActiveOrder(shipper.getId());

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (!WAITING_FOR_SHIPPER_STATUSES.contains(order.getStatus()) || order.getShipperId() != null) {
            throw new BadRequestException("Order is not waiting for shipper");
        }
        if (hasCancelledThisOrder(order.getId(), shipper.getId())) {
            throw new BadRequestException("Current shipper cancelled this order before");
        }

        BigDecimal distanceKm = calculateDistanceToRestaurant(order, profile);
        if (distanceKm.compareTo(MAX_PICKUP_DISTANCE_KM) > 0) {
            throw new BadRequestException("Order is outside 5km pickup radius");
        }

        order.setShipperId(shipper.getId());
        profile.setCurrentStatus(ShipperStatus.BUSY);

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setOrder(order);
        assignment.setShipper(shipper);
        assignment.setStatus(DeliveryAssignmentStatus.ACCEPTED);
        assignment.setDistanceKm(distanceKm);
        assignment.setAcceptedAt(LocalDateTime.now());

        shipperProfileRepository.save(profile);
        deliveryAssignmentRepository.save(assignment);
        return toOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(String username, Long orderId, ShipperCancelOrderRequest request) {
        User shipper = getUser(username);
        ShipperProfile profile = getOrCreateProfile(shipper);
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!shipper.getId().equals(order.getShipperId())) {
            throw new BadRequestException("This order is not assigned to current shipper");
        }
        if (order.getStatus() == OrderStatus.DELIVERING) {
            throw new BadRequestException("Cannot cancel after delivery has started");
        }
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel a finished order");
        }

        DeliveryAssignment assignment = deliveryAssignmentRepository
                .findFirstByOrder_IdAndShipper_IdAndStatusOrderByCreatedAtDesc(
                        order.getId(), shipper.getId(), DeliveryAssignmentStatus.ACCEPTED)
                .orElseThrow(() -> new BadRequestException("Accepted assignment not found"));

        ShipperStatus statusAfterCancel = request.statusAfterCancel() == null
                ? ShipperStatus.AVAILABLE
                : request.statusAfterCancel();
        if (statusAfterCancel == ShipperStatus.BUSY) {
            throw new BadRequestException("BUSY status is managed by delivery workflow");
        }

        assignment.setStatus(DeliveryAssignmentStatus.CANCELLED);
        assignment.setCancelReason(request.reason());
        assignment.setCancelledAt(LocalDateTime.now());

        order.setShipperId(null);
        profile.setCurrentStatus(statusAfterCancel);

        deliveryAssignmentRepository.save(assignment);
        shipperProfileRepository.save(profile);
        Order savedOrder = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderWaitingForShipperEvent(savedOrder.getId()));
        return toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipperProfile> findAvailableShippersForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (order.getShipperId() != null || !WAITING_FOR_SHIPPER_STATUSES.contains(order.getStatus())) {
            return List.of();
        }

        Restaurant restaurant = getRestaurant(order);
        LocalDateTime minimumLocationTime = LocalDateTime.now().minusMinutes(ACTIVE_LOCATION_MINUTES);
        return shipperProfileRepository
                .findByCurrentStatusAndCurrentLatitudeIsNotNullAndCurrentLongitudeIsNotNullAndLastLocationAtAfter(
                        ShipperStatus.AVAILABLE,
                        minimumLocationTime)
                .stream()
                .filter(profile -> !hasCancelledThisOrder(order.getId(), profile.getUser().getId()))
                .filter(profile -> calculateDistance(profile, restaurant).compareTo(MAX_PICKUP_DISTANCE_KM) <= 0)
                .sorted(Comparator.comparing(profile -> calculateDistance(profile, restaurant)))
                .toList();
    }

    private NearbyOrderResponse toNearbyOrder(Order order, ShipperProfile profile) {
        return new NearbyOrderResponse(toOrderResponse(order), calculateDistanceToRestaurant(order, profile));
    }

    private BigDecimal calculateDistanceToRestaurant(Order order, ShipperProfile profile) {
        return calculateDistance(profile, getRestaurant(order));
    }

    private BigDecimal calculateDistance(ShipperProfile profile, Restaurant restaurant) {
        return feeService.calculateDistanceKm(
                profile.getCurrentLatitude(),
                profile.getCurrentLongitude(),
                restaurant.getLatitude(),
                restaurant.getLongitude());
    }

    private Restaurant getRestaurant(Order order) {
        return restaurantRepository.findById(order.getRestaurantId())
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private ShipperProfile getRequiredReadyProfile(User shipper) {
        ShipperProfile profile = getOrCreateProfile(shipper);
        if (profile.getCurrentStatus() != ShipperStatus.AVAILABLE) {
            throw new BadRequestException("Shipper must be AVAILABLE");
        }
        if (profile.getCurrentLatitude() == null || profile.getCurrentLongitude() == null) {
            throw new BadRequestException("Shipper location is required");
        }
        if (profile.getLastLocationAt() == null
                || profile.getLastLocationAt().isBefore(LocalDateTime.now().minusMinutes(ACTIVE_LOCATION_MINUTES))) {
            throw new BadRequestException("Shipper location is expired");
        }
        return profile;
    }

    private ShipperProfile getOrCreateProfile(User user) {
        return shipperProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> shipperProfileRepository.save(createProfile(user)));
    }

    private ShipperProfile createProfile(User user) {
        ShipperProfile profile = new ShipperProfile();
        profile.setUser(user);
        profile.setCurrentStatus(ShipperStatus.AVAILABLE);
        return profile;
    }

    private void ensureNoActiveOrder(Long shipperId) {
        if (!orderRepository.findByShipperIdAndStatusInOrderByCreatedAtDesc(
                shipperId,
                SHIPPER_ACTIVE_ORDER_STATUSES).isEmpty()) {
            throw new BadRequestException("Shipper already has an active order");
        }
    }

    private boolean hasCancelledThisOrder(Long orderId, Long shipperId) {
        return deliveryAssignmentRepository.existsByOrder_IdAndShipper_IdAndStatusIn(
                orderId,
                shipperId,
                HIDDEN_AFTER_CANCEL_STATUSES);
    }

    private OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .restaurantId(order.getRestaurantId())
                .customerId(order.getCustomerId())
                .shipperId(order.getShipperId())
                .voucherId(order.getVoucherId())
                .status(order.getStatus())
                .itemsTotal(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .deliveryFee(order.getDeliveryFee())
                .finalAmount(order.getFinalAmount())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .deliveryAddress(order.getDeliveryAddress())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(this::toOrderItemResponse).toList())
                .build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItemId())
                .itemName(item.getItemNameSnapshot())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build();
    }
}
