package com.baemin_mini.service.impl;

import com.baemin_mini.common.exception.BadRequestException;
import com.baemin_mini.common.exception.NotFoundException;
import com.baemin_mini.domain.entity.DeliveryAssignment;
import com.baemin_mini.domain.entity.Order;
import com.baemin_mini.domain.entity.OrderItem;
import com.baemin_mini.domain.entity.OrderTracking;
import com.baemin_mini.domain.entity.ShipperProfile;
import com.baemin_mini.domain.entity.User;
import com.baemin_mini.domain.enums.DeliveryAssignmentStatus;
import com.baemin_mini.domain.enums.OrderStatus;
import com.baemin_mini.domain.enums.PaymentMethod;
import com.baemin_mini.domain.enums.PaymentStatus;
import com.baemin_mini.domain.enums.RoleName;
import com.baemin_mini.domain.enums.ShipperStatus;
import com.baemin_mini.dto.order.OrderItemResponse;
import com.baemin_mini.dto.order.OrderResponse;
import com.baemin_mini.dto.shipper.ShipperLocationRequest;
import com.baemin_mini.dto.shipper.ShipperProfileResponse;
import com.baemin_mini.repository.DeliveryAssignmentRepository;
import com.baemin_mini.repository.OrderRepository;
import com.baemin_mini.repository.ShipperProfileRepository;
import com.baemin_mini.repository.UserRepository;
import com.baemin_mini.service.DeliveryDispatchService;
import com.baemin_mini.service.ShipperProfileService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShipperProfileServiceImpl implements ShipperProfileService {
    private static final List<OrderStatus> ACTIVE_DELIVERY_STATUSES = List.of(
            OrderStatus.PLACED,
            OrderStatus.PREPARING,
            OrderStatus.DELIVERING);
    private final UserRepository userRepository;
    private final ShipperProfileRepository shipperProfileRepository;
    private final OrderRepository orderRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final DeliveryDispatchService deliveryDispatchService;


    @Override
    @Transactional
    public ShipperProfileResponse getProfile(String username) {
        User user = getUser(username);
        return toResponse(getOrCreateProfile(user));
    }

    @Override
    @Transactional
    public ShipperProfileResponse updateLocation(String username, ShipperLocationRequest request) {
        User user = getUser(username);
        ShipperProfile profile = getOrCreateProfile(user);

        profile.setCurrentLatitude(request.latitude());
        profile.setCurrentLongitude(request.longitude());
        profile.setLastLocationAt(LocalDateTime.now());
        ShipperProfile savedProfile = shipperProfileRepository.save(profile);
        dispatchWaitingOrdersIfReady(username, savedProfile);
        return toResponse(savedProfile);
    }

    @Override
    @Transactional
    public ShipperProfileResponse updateStatus(String username, ShipperStatus status) {
        User user = getUser(username);
        ShipperProfile profile = getOrCreateProfile(user);

        if (status == ShipperStatus.BUSY) {
            throw new BadRequestException("BUSY status is managed by delivery workflow");
        }
        if (status == ShipperStatus.OFFLINE && hasActiveDelivery(user.getId())) {
            throw new BadRequestException("Cannot go offline while having an active order");
        }

        profile.setCurrentStatus(status);
        ShipperProfile savedProfile = shipperProfileRepository.save(profile);
        dispatchWaitingOrdersIfReady(username, savedProfile);
        return toResponse(savedProfile);
    }
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyActiveOrders(String username) {
        User user = getUser(username);
        return orderRepository.findByShipperIdAndStatusInOrderByCreatedAtDesc(user.getId(), ACTIVE_DELIVERY_STATUSES)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse startDelivery(String username, Long orderId) {
        User user = getUser(username);
        ShipperProfile profile = getOrCreateProfile(user);
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!user.getId().equals(order.getShipperId())) {
            throw new BadRequestException("This order is not assigned to current shipper");
        }
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new BadRequestException("Order must be PREPARING before delivery starts");
        }

        order.setStatus(OrderStatus.DELIVERING);
        order.addTracking(OrderTracking.builder()
                .status(OrderStatus.DELIVERING)
                .actorRole(RoleName.SHIPPER.name())
                .actorId(user.getId())
                .note("Shipper started delivery")
                .build());

        profile.setCurrentStatus(ShipperStatus.BUSY);
        shipperProfileRepository.save(profile);
        return toOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse completeDelivery(String username, Long orderId) {
        User user = getUser(username);
        ShipperProfile profile = getOrCreateProfile(user);
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!user.getId().equals(order.getShipperId())) {
            throw new BadRequestException("This order is not assigned to current shipper");
        }
        if (order.getStatus() != OrderStatus.DELIVERING) {
            throw new BadRequestException("Order is not being delivered");
        }

        order.setStatus(OrderStatus.DELIVERED);
        if (order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }
        order.addTracking(OrderTracking.builder()
                .status(OrderStatus.DELIVERED)
                .actorRole(RoleName.SHIPPER.name())
                .actorId(user.getId())
                .note("Shipper completed delivery")
                .build());

        completeAcceptedAssignment(order.getId(), user.getId());
        profile.setCurrentStatus(ShipperStatus.AVAILABLE);
        shipperProfileRepository.save(profile);
        return toOrderResponse(orderRepository.save(order));
    }


    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private ShipperProfile getOrCreateProfile(User user) {
        return shipperProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> shipperProfileRepository.save(createProfile(user)));
    }

    private boolean hasActiveDelivery(Long shipperId) {
        return !orderRepository.findByShipperIdAndStatusInOrderByCreatedAtDesc(shipperId, ACTIVE_DELIVERY_STATUSES).isEmpty();
    }

    private void dispatchWaitingOrdersIfReady(String username, ShipperProfile profile) {
        if (profile.getCurrentStatus() == ShipperStatus.AVAILABLE && hasFreshLocation(profile)) {
            deliveryDispatchService.dispatchWaitingOrdersToShipper(username);
        }
    }

    private boolean hasFreshLocation(ShipperProfile profile) {
        return profile.getCurrentLatitude() != null
                && profile.getCurrentLongitude() != null
                && profile.getLastLocationAt() != null
                && !profile.getLastLocationAt().isBefore(LocalDateTime.now().minusMinutes(30));
    }

    private ShipperProfile createProfile(User user) {
        ShipperProfile profile = new ShipperProfile();
        profile.setUser(user);
        profile.setCurrentStatus(ShipperStatus.AVAILABLE);
        return profile;
    }

    private void completeAcceptedAssignment(Long orderId, Long shipperId) {
        DeliveryAssignment assignment = deliveryAssignmentRepository
                .findFirstByOrder_IdAndShipper_IdAndStatusOrderByCreatedAtDesc(
                        orderId, shipperId, DeliveryAssignmentStatus.ACCEPTED)
                .orElseThrow(() -> new BadRequestException("Accepted assignment not found"));
        assignment.setStatus(DeliveryAssignmentStatus.COMPLETED);
        assignment.setCompletedAt(LocalDateTime.now());
        deliveryAssignmentRepository.save(assignment);
    }

    private ShipperProfileResponse toResponse(ShipperProfile profile) {
        return new ShipperProfileResponse(
                profile.getId(),
                profile.getCurrentStatus(),
                profile.getCurrentLatitude(),
                profile.getCurrentLongitude(),
                profile.getLastLocationAt());
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
