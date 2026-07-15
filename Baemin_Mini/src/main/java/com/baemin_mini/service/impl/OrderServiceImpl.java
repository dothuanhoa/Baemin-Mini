package com.baemin_mini.service.impl;

import com.baemin_mini.common.exception.BadRequestException;
import com.baemin_mini.common.exception.ForbiddenException;
import com.baemin_mini.common.exception.NotFoundException;
import com.baemin_mini.domain.entity.MenuItem;
import com.baemin_mini.domain.entity.Order;
import com.baemin_mini.domain.entity.OrderItem;
import com.baemin_mini.domain.entity.OrderTracking;
import com.baemin_mini.domain.entity.Restaurant;
import com.baemin_mini.domain.entity.User;
import com.baemin_mini.domain.enums.OrderStatus;
import com.baemin_mini.domain.enums.PaymentMethod;
import com.baemin_mini.domain.enums.PaymentStatus;
import com.baemin_mini.domain.enums.RoleName;
import com.baemin_mini.dto.order.OrderFeeRequest;
import com.baemin_mini.dto.order.OrderFeeResponse;
import com.baemin_mini.dto.order.OrderItemRequest;
import com.baemin_mini.dto.order.OrderItemResponse;
import com.baemin_mini.dto.order.OrderRequest;
import com.baemin_mini.dto.order.OrderResponse;
import com.baemin_mini.dto.order.OrderTrackingResponse;
import com.baemin_mini.dto.voucher.VoucherApplyRequest;
import com.baemin_mini.dto.voucher.VoucherApplyResponse;
import com.baemin_mini.repository.MenuItemRepository;
import com.baemin_mini.repository.OrderRepository;
import com.baemin_mini.repository.OrderTrackingRepository;
import com.baemin_mini.repository.RestaurantRepository;
import com.baemin_mini.repository.UserRepository;
import com.baemin_mini.service.FeeService;
import com.baemin_mini.service.OrderService;
import com.baemin_mini.service.VoucherService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final List<OrderStatus> RESTAURANT_VISIBLE_STATUSES = List.of(
            OrderStatus.PLACED,
            OrderStatus.PREPARING);

    private final OrderRepository orderRepository;
    private final OrderTrackingRepository orderTrackingRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final FeeService feeService;
    private final VoucherService voucherService;

    @Override
    @Transactional(readOnly = true)
    public OrderFeeResponse calculateFee(OrderFeeRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));

        BigDecimal distanceKm = feeService.calculateDistanceKm(
                restaurant.getLatitude(), restaurant.getLongitude(),
                request.getLatitude(), request.getLongitude()
        );

        BigDecimal deliveryFee = feeService.calculateDeliveryFee(distanceKm, request.getItemsTotal());

        return OrderFeeResponse.builder()
                .distanceKm(distanceKm)
                .deliveryFee(deliveryFee)
                .build();
    }

    @Override
    @Transactional
    public OrderResponse createOrder(String username, OrderRequest request) {
        User customer = getCurrentUser(username);
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));

        if (Boolean.FALSE.equals(restaurant.getIsOpen())) {
            throw new BadRequestException("Restaurant is currently closed");
        }

        BigDecimal itemsTotal = BigDecimal.ZERO;
        Order order = Order.builder()
                .customerId(customer.getId())
                .restaurantId(restaurant.getId())
                .receiverName(customer.getFullName())
                .receiverPhone(customer.getPhone() == null || customer.getPhone().isBlank() ? "N/A" : customer.getPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .restaurantNote(request.getRestaurantNote())
                .shipperNote(request.getShipperNote())
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.UNPAID)
                .status(OrderStatus.PLACED)
                .build();

        for (OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new NotFoundException("Menu item not found: " + itemReq.getMenuItemId()));

            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new BadRequestException("All items must be from the same restaurant");
            }

            BigDecimal lineTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            itemsTotal = itemsTotal.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .menuItemId(menuItem.getId())
                    .itemNameSnapshot(menuItem.getName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .build();
            order.addItem(orderItem);
        }

        order.setTotalAmount(itemsTotal);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            VoucherApplyRequest voucherRequest = new VoucherApplyRequest();
            voucherRequest.setCode(request.getVoucherCode());
            voucherRequest.setItemsTotal(itemsTotal);

            VoucherApplyResponse voucherResponse = voucherService.applyVoucher(voucherRequest);
            discountAmount = voucherResponse.getDiscountAmount();
            order.setVoucherId(voucherResponse.getVoucherId());
        }
        order.setDiscountAmount(discountAmount);

        BigDecimal distanceKm = feeService.calculateDistanceKm(
                restaurant.getLatitude(), restaurant.getLongitude(),
                request.getLatitude(), request.getLongitude()
        );
        BigDecimal deliveryFee = feeService.calculateDeliveryFee(distanceKm, itemsTotal);
        order.setDeliveryFee(deliveryFee);

        BigDecimal amountAfterDiscount = itemsTotal.subtract(discountAmount);
        if (amountAfterDiscount.compareTo(BigDecimal.ZERO) < 0) {
            amountAfterDiscount = BigDecimal.ZERO;
        }
        order.setFinalAmount(amountAfterDiscount.add(deliveryFee));

        BigDecimal platformFee = amountAfterDiscount.multiply(restaurant.getCommissionRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        order.setPlatformFee(platformFee);
        order.setShipperEarning(deliveryFee);

        OrderTracking tracking = OrderTracking.builder()
                .status(OrderStatus.PLACED)
                .actorRole(RoleName.CUSTOMER.name())
                .actorId(customer.getId())
                .note("Customer placed order")
                .build();
        order.addTracking(tracking);

        return mapToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String username) {
        User customer = getCurrentUser(username);
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId())
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getRestaurantOrders(String username, Long restaurantId) {
        User actor = getCurrentUser(username);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
        if (!hasRole(actor, RoleName.ADMIN) && !restaurant.getOwner().getId().equals(actor.getId())) {
            throw new ForbiddenException("You do not have permission to view this restaurant orders");
        }
        return orderRepository.findByRestaurantIdAndStatusInOrderByCreatedAtDesc(restaurantId, RESTAURANT_VISIBLE_STATUSES)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderTrackingResponse> getOrderTracking(String username, Long orderId) {
        User actor = getCurrentUser(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        assertCanViewOrder(actor, order);

        return orderTrackingRepository.findByOrderIdOrderByCreatedAtAsc(orderId)
                .stream().map(t -> OrderTrackingResponse.builder()
                        .status(t.getStatus())
                        .note(t.getNote())
                        .actorRole(t.getActorRole())
                        .createdAt(t.getCreatedAt())
                        .build()).toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String username, Long orderId, String statusStr) {
        User actor = getCurrentUser(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        OrderStatus newStatus = parseStatus(statusStr);
        assertCanUpdateStatus(actor, order, newStatus);
        assertValidTransition(order.getStatus(), newStatus);

        if (newStatus == OrderStatus.DELIVERING && order.getShipperId() == null && hasRole(actor, RoleName.SHIPPER)) {
            order.setShipperId(actor.getId());
        }
        if (newStatus == OrderStatus.DELIVERED && order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }
        if (newStatus == OrderStatus.CANCELLED) {
            order.setPaymentStatus(PaymentStatus.CANCELLED);
        }

        order.setStatus(newStatus);

        OrderTracking tracking = OrderTracking.builder()
                .status(newStatus)
                .actorRole(primaryRole(actor).name())
                .actorId(actor.getId())
                .note("Order status changed to " + newStatus)
                .build();
        order.addTracking(tracking);

        return mapToResponse(orderRepository.save(order));
    }

    private User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private OrderStatus parseStatus(String statusStr) {
        try {
            return OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid order status");
        }
    }

    private void assertCanViewOrder(User actor, Order order) {
        if (hasRole(actor, RoleName.ADMIN)) {
            return;
        }
        if (hasRole(actor, RoleName.CUSTOMER) && order.getCustomerId().equals(actor.getId())) {
            return;
        }
        if (hasRole(actor, RoleName.RESTAURANT) && ownsRestaurant(actor, order.getRestaurantId())) {
            return;
        }
        if (hasRole(actor, RoleName.SHIPPER) && actor.getId().equals(order.getShipperId())) {
            return;
        }
        throw new ForbiddenException("You do not have permission to view this order");
    }

    private void assertCanUpdateStatus(User actor, Order order, OrderStatus newStatus) {
        if (hasRole(actor, RoleName.ADMIN)) {
            return;
        }
        if (hasRole(actor, RoleName.RESTAURANT)) {
            if (!ownsRestaurant(actor, order.getRestaurantId())) {
                throw new ForbiddenException("You do not have permission to update this order");
            }
            if (EnumSet.of(OrderStatus.PREPARING, OrderStatus.CANCELLED).contains(newStatus)) {
                return;
            }
        }
        if (hasRole(actor, RoleName.SHIPPER)) {
            boolean canClaimOrder = newStatus == OrderStatus.DELIVERING && order.getShipperId() == null;
            boolean canUpdateOwnOrder = actor.getId().equals(order.getShipperId())
                    && EnumSet.of(OrderStatus.DELIVERING, OrderStatus.DELIVERED).contains(newStatus);
            if (canClaimOrder || canUpdateOwnOrder) {
                return;
            }
        }
        throw new ForbiddenException("You do not have permission to update this order status");
    }

    private void assertValidTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }
        boolean valid = switch (currentStatus) {
            case PLACED -> newStatus == OrderStatus.PREPARING || newStatus == OrderStatus.CANCELLED;
            case PREPARING -> newStatus == OrderStatus.DELIVERING || newStatus == OrderStatus.CANCELLED;
            case DELIVERING -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
        if (!valid) {
            throw new BadRequestException("Invalid order status transition");
        }
    }

    private boolean ownsRestaurant(User actor, Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .map(restaurant -> restaurant.getOwner().getId().equals(actor.getId()))
                .orElse(false);
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getName() == roleName);
    }

    private RoleName primaryRole(User user) {
        if (hasRole(user, RoleName.ADMIN)) {
            return RoleName.ADMIN;
        }
        if (hasRole(user, RoleName.RESTAURANT)) {
            return RoleName.RESTAURANT;
        }
        if (hasRole(user, RoleName.SHIPPER)) {
            return RoleName.SHIPPER;
        }
        return RoleName.CUSTOMER;
    }

    private OrderResponse mapToResponse(Order order) {
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
                .items(order.getItems().stream().map(i -> OrderItemResponse.builder()
                        .id(i.getId())
                        .menuItemId(i.getMenuItemId())
                        .itemName(i.getItemNameSnapshot())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build()).toList())
                .build();
    }
}
