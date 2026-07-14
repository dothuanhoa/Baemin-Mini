package com.baemin_mini.service.impl;

import com.baemin_mini.common.exception.BadRequestException;
import com.baemin_mini.common.exception.NotFoundException;
import com.baemin_mini.domain.entity.Order;
import com.baemin_mini.domain.entity.OrderItem;
import com.baemin_mini.domain.entity.OrderTracking;
import com.baemin_mini.domain.entity.Restaurant;
import com.baemin_mini.domain.entity.MenuItem;
import com.baemin_mini.domain.enums.OrderStatus;
import com.baemin_mini.domain.enums.PaymentMethod;
import com.baemin_mini.domain.enums.PaymentStatus;
import com.baemin_mini.dto.OrderFeeRequest;
import com.baemin_mini.dto.OrderFeeResponse;
import com.baemin_mini.dto.OrderItemRequest;
import com.baemin_mini.dto.OrderItemResponse;
import com.baemin_mini.dto.OrderRequest;
import com.baemin_mini.dto.OrderResponse;
import com.baemin_mini.dto.OrderTrackingResponse;
import com.baemin_mini.dto.VoucherApplyRequest;
import com.baemin_mini.dto.VoucherApplyResponse;
import com.baemin_mini.repository.MenuItemRepository;
import com.baemin_mini.repository.OrderRepository;
import com.baemin_mini.repository.OrderTrackingRepository;
import com.baemin_mini.repository.RestaurantRepository;
import com.baemin_mini.service.FeeService;
import com.baemin_mini.service.OrderService;
import com.baemin_mini.service.ShipperAssignmentService;
import com.baemin_mini.service.SseService;
import com.baemin_mini.service.VoucherService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderTrackingRepository orderTrackingRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final FeeService feeService;
    private final VoucherService voucherService;
    private final ShipperAssignmentService shipperAssignmentService;
    private final SseService sseService;

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
    public OrderResponse createOrder(Long customerId, OrderRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));

        if (Boolean.FALSE.equals(restaurant.getIsOpen())) {
            throw new BadRequestException("Restaurant is currently closed");
        }

        // Calculate items total and build order items
        BigDecimal itemsTotal = BigDecimal.ZERO;
        Order order = Order.builder()
                .customerId(customerId)
                .restaurantId(restaurant.getId())
                .receiverName("Customer " + customerId) // Tạm thời dùng ID do chưa có bảng User Profile
                .receiverPhone("0123456789")
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

        // Calculate Discount
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            VoucherApplyRequest vReq = new VoucherApplyRequest();
            vReq.setCode(request.getVoucherCode());
            vReq.setItemsTotal(itemsTotal);
            
            VoucherApplyResponse vRes = voucherService.applyVoucher(vReq);
            discountAmount = vRes.getDiscountAmount();
        }
        order.setDiscountAmount(discountAmount);

        // Calculate Delivery Fee
        BigDecimal distanceKm = feeService.calculateDistanceKm(
                restaurant.getLatitude(), restaurant.getLongitude(),
                request.getLatitude(), request.getLongitude()
        );
        BigDecimal deliveryFee = feeService.calculateDeliveryFee(distanceKm, itemsTotal);
        order.setDeliveryFee(deliveryFee);

        // Calculate Final Amount
        BigDecimal amountAfterDiscount = itemsTotal.subtract(discountAmount);
        if (amountAfterDiscount.compareTo(BigDecimal.ZERO) < 0) {
            amountAfterDiscount = BigDecimal.ZERO;
        }
        BigDecimal finalAmount = amountAfterDiscount.add(deliveryFee);
        order.setFinalAmount(finalAmount);

        // Calculate Platform Fee & Shipper Earning
        BigDecimal platformFee = amountAfterDiscount.multiply(restaurant.getCommissionRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        order.setPlatformFee(platformFee);
        order.setShipperEarning(deliveryFee);

        Order savedOrder = orderRepository.save(order);

        // Add Tracking
        OrderTracking tracking = OrderTracking.builder()
                .status(OrderStatus.PLACED)
                .actorRole("CUSTOMER")
                .actorId(customerId)
                .note("Khách hàng đặt đơn")
                .build();
        savedOrder.addTracking(tracking);
        // Note: tracking is cascade saved via order

        // Notify Restaurant via SSE
        sseService.notifyRestaurant(restaurant.getId(), "NEW_ORDER", "You have a new order: " + savedOrder.getId());

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getRestaurantOrders(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderTrackingResponse> getOrderTracking(Long orderId) {
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
    public OrderResponse updateOrderStatus(Long orderId, String statusStr, Long actorId, String actorRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status");
        }

        order.setStatus(newStatus);
        
        OrderTracking tracking = OrderTracking.builder()
                .status(newStatus)
                .actorRole(actorRole)
                .actorId(actorId)
                .note("Cập nhật trạng thái thành " + newStatus)
                .build();
        order.addTracking(tracking);

        Order savedOrder = orderRepository.save(order);

        if (newStatus == OrderStatus.READY_FOR_PICKUP) {
            shipperAssignmentService.assignNearestShipper(savedOrder.getId());
        }

        return mapToResponse(savedOrder);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .restaurantId(order.getRestaurantId())
                .customerId(order.getCustomerId())
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
